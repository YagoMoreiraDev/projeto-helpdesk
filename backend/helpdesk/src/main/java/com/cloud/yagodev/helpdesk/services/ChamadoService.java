package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.*;
import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.entities.ChamadoEvento;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.NotificationType;
import com.cloud.yagodev.helpdesk.enums.Role;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.repositories.ChamadoRepository;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChamadoService {

    private final UsuarioRepository usuarioRepo;
    private final ChamadoRepository chamadoRepo;
    private final NotificationService notifications;

    private final AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
    private void touch() { lastUpdate.set(System.currentTimeMillis()); }
    public long getLastUpdate() { return lastUpdate.get(); }

    public ChamadoService(UsuarioRepository usuarioRepo,
                          ChamadoRepository chamadoRepo,
                          NotificationService notifications) {
        this.usuarioRepo = usuarioRepo;
        this.chamadoRepo = chamadoRepo;
        this.notifications = notifications;
    }

    @Transactional
    public Chamado abrirChamado(UUID solicitanteId, ChamadoCreateRequest req) {
        Usuario solicitante = getUsuario(solicitanteId);
        if (!solicitante.getRoles().contains(Role.USUARIO_COMUM)
                && !solicitante.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Apenas USUARIO_COMUM ou ADMIN podem abrir chamados.");
        }
        Chamado c = new Chamado(solicitante, req.titulo(), req.descricao(), req.prioridade());
        c.comentar(solicitante, "Chamado criado por " + solicitante.getNome());
        c.alterarStatus(StatusChamado.ABERTO, solicitante, "Status inicial: ABERTO");
        c = chamadoRepo.save(c);

        // Notifica ADMIN + todos os TÉCNICOS + o próprio solicitante (para atualizar a lista dele)
        var dto = NotificationDto.of(
                NotificationType.CHAMADO_CRIADO,
                "Novo chamado: " + c.getTitulo(),
                toResponse(c)
        );
        notifications.sendToRole(Role.ADMIN, dto);
        notifications.sendToRole(Role.TECNICO, dto);
        notifications.sendToUser(solicitante.getId(), dto);
        touch();
        return c;
    }

    @Transactional
    public Chamado assumir(UUID tecnicoId, UUID chamadoId) {
        Usuario tecnico = getUsuario(tecnicoId);
        if (!tecnico.getRoles().contains(Role.TECNICO) && !tecnico.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Somente TECNICO ou ADMIN podem assumir chamados.");
        }
        Chamado c = getChamado(chamadoId);
        c.atribuirTecnico(tecnico, tecnico);

        // Notifica o técnico que assumiu (útil se ele estiver em outra aba/dispositivo)
        var dto = NotificationDto.of(
                NotificationType.CHAMADO_ATRIBUIDO,
                "Você assumiu: " + c.getTitulo(),
                toResponse(c)
        );
        notifications.sendToUser(tecnico.getId(), dto);

        touch();
        return c;
    }

    @Transactional
    public Chamado alterarStatus(UUID autorId, UUID chamadoId, StatusChangeRequest req) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);

        if (req.novoStatus() == StatusChamado.CONCLUIDO) {
            boolean pode = (c.getTecnicoResponsavel() != null
                    && c.getTecnicoResponsavel().getId().equals(autor.getId()))
                    || autor.getRoles().contains(Role.ADMIN);
            if (!pode) throw new IllegalStateException("Apenas o técnico responsável ou ADMIN podem concluir.");
        }
        StatusChamado anterior = c.getStatus();
        c.alterarStatus(req.novoStatus(), autor, req.detalhe());

        // (Opcional) Notifique mudança de status para ADMIN, técnico responsável e solicitante
        var dto = NotificationDto.of(
                NotificationType.STATUS_ALTERADO,
                "Status alterado de " + anterior + " para " + c.getStatus(),
                toResponse(c)
        );
        notifications.sendToRole(Role.ADMIN, dto);
        if (c.getTecnicoResponsavel() != null) notifications.sendToUser(c.getTecnicoResponsavel().getId(), dto);
        if (c.getSolicitante() != null) notifications.sendToUser(c.getSolicitante().getId(), dto);

        touch();
        return c;
    }

    @Transactional
    public Chamado comentar(UUID autorId, UUID chamadoId, ComentarioRequest req) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);
        c.comentar(autor, req.mensagem());

        touch();
        return c;
    }

    @Transactional
    public void excluirChamado(UUID autorId, UUID chamadoId) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);

        boolean isAdmin = autor.getRoles().contains(Role.ADMIN);
        boolean isDono  = c.getSolicitante() != null
                && c.getSolicitante().getId().equals(autor.getId());

        if (!isAdmin && !isDono) {
            throw new AccessDeniedException("Você não pode excluir este chamado.");
        }
        if (!isAdmin) { // regras extras para o solicitante
            if (c.getStatus() != StatusChamado.ABERTO) {
                throw new IllegalStateException("Somente chamados EM_ABERTO podem ser excluídos pelo solicitante.");
            }
            if (c.getTecnicoResponsavel() != null) {
                throw new IllegalStateException("Chamado com técnico atribuído não pode ser excluído pelo solicitante.");
            }
        }
        touch();
        chamadoRepo.delete(c);
    }

    /** ADMIN designa qualquer técnico para um chamado. */
    @Transactional
    public Chamado designarTecnico(UUID adminId, UUID chamadoId, UUID tecnicoId) {
        Usuario admin = getUsuario(adminId);
        if (!admin.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Somente ADMIN pode designar técnico explicitamente.");
        }
        Usuario tecnico = getUsuario(tecnicoId);
        if (!tecnico.getRoles().contains(Role.TECNICO)) {
            throw new IllegalArgumentException("Destino não é um técnico.");
        }
        Chamado c = getChamado(chamadoId);
        c.atribuirTecnico(tecnico, admin); // já registra evento e põe EM_ATENDIMENTO se estava ABERTO

        // Notifica o técnico designado
        var dto = NotificationDto.of(
                NotificationType.CHAMADO_ATRIBUIDO,
                "Você foi designado: " + c.getTitulo(),
                toResponse(c)
        );
        notifications.sendToUser(tecnico.getId(), dto);

        touch();
        return c;
    }

    /** Cancelar em vez de deletar: cliente cancela sob regras; ADMIN cancela sempre. */
    @Transactional
    public Chamado cancelarChamado(UUID autorId, UUID chamadoId, String detalhe) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);

        boolean isAdmin = autor.getRoles().contains(Role.ADMIN);
        boolean isDono  = c.getSolicitante() != null && c.getSolicitante().getId().equals(autor.getId());

        if (!isAdmin && !isDono) throw new AccessDeniedException("Sem permissão para cancelar.");

        if (!isAdmin) {
            if (c.getStatus() != StatusChamado.ABERTO)
                throw new IllegalStateException("Só é possível cancelar chamados ABERTO.");
            if (c.getTecnicoResponsavel() != null)
                throw new IllegalStateException("Chamado com técnico atribuído não pode ser cancelado pelo solicitante.");
        }

        c.alterarStatus(StatusChamado.CANCELADO, autor, detalhe != null ? detalhe : "Cancelado.");

        // Notifica ADMIN e técnico responsável (se houver) e solicitante
        var dto = NotificationDto.of(
                NotificationType.STATUS_ALTERADO,
                "Chamado cancelado.",
                toResponse(c)
        );
        notifications.sendToRole(Role.ADMIN, dto);
        if (c.getTecnicoResponsavel() != null) notifications.sendToUser(c.getTecnicoResponsavel().getId(), dto);
        if (c.getSolicitante() != null) notifications.sendToUser(c.getSolicitante().getId(), dto);

        touch();
        return c;
    }

    /** Listar em aberto e sem técnico (útil para técnico e admin). */
    @Transactional(readOnly = true)
    public List<Chamado> listarSemTecnicoEmAberto() {
        return chamadoRepo.findAll().stream()
                .filter(c -> c.getStatus() == StatusChamado.ABERTO && c.getTecnicoResponsavel() == null)
                .toList();
    }

    /** Filtro por status para telas do ADMIN. */
    @Transactional(readOnly = true)
    public List<Chamado> listarPorStatus(StatusChamado st) {
        return chamadoRepo.findByStatus(st);
    }

    @Transactional(readOnly = true)
    public List<Chamado> listarMeusChamados(UUID solicitanteId) {
        return chamadoRepo.findBySolicitanteId(solicitanteId);
    }

    @Transactional(readOnly = true)
    public List<Chamado> listarPorTecnico(UUID tecnicoId) {
        return chamadoRepo.findByTecnicoResponsavelId(tecnicoId);
    }

    @Transactional(readOnly = true)
    public List<Chamado> listarTodos() {
        return chamadoRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Chamado> listarEmAberto() {
        return chamadoRepo.findByStatus(StatusChamado.ABERTO);
    }

    // ------- helpers -------

    private Usuario getUsuario(UUID id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    private Chamado getChamado(UUID id) {
        return chamadoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Chamado não encontrado"));
    }

    /** Copiado do Controller para permitir empurrar o chamado inteiro via SSE. */
    private ChamadoResponse toResponse(Chamado c) {
        var eventos = c.getEventos().stream().map(ev -> new ChamadoEventoResponse(
                ev.getId(),
                ev.getQuando(),
                ev.getTipo(),
                ev.getAutor() != null ? ev.getAutor().getNome() : null,
                safeDetalhe(ev),
                ev.getStatusAnterior(),
                ev.getStatusNovo()
        )).toList();

        return new ChamadoResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescricao(),
                c.getStatus(),
                c.getPrioridade(),
                c.getSolicitante() != null ? c.getSolicitante().getId() : null,
                c.getSolicitante() != null ? c.getSolicitante().getNome() : null,
                c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getId() : null,
                c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : null,
                c.getCreatedAt(),
                c.getClosedAt(),
                eventos
        );
    }

    private String safeDetalhe(ChamadoEvento ev) {
        try {
            Method m = ev.getClass().getMethod("getDetalhe");
            return (String) m.invoke(ev);
        } catch (Exception ignore) {
            try {
                Method m2 = ev.getClass().getMethod("getDetallhe");
                return (String) m2.invoke(ev);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
