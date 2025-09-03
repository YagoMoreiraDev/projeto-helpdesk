package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.ChamadoCreateRequest;
import com.cloud.yagodev.helpdesk.dtos.ComentarioRequest;
import com.cloud.yagodev.helpdesk.dtos.StatusChangeRequest;
import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.Prioridade;
import com.cloud.yagodev.helpdesk.enums.Role;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.repositories.ChamadoRepository;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ChamadoService {

    private final UsuarioRepository usuarioRepo;
    private final ChamadoRepository chamadoRepo;

    public ChamadoService(UsuarioRepository usuarioRepo, ChamadoRepository chamadoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.chamadoRepo = chamadoRepo;
    }

    @Transactional
    public Chamado abrirChamado(UUID solicitanteId, ChamadoCreateRequest req) {
        Usuario solicitante = getUsuario(solicitanteId);
        if (!solicitante.getRoles().contains(Role.USUARIO_COMUM) && !solicitante.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Apenas USUARIO_COMUM ou ADMIN podem abrir chamados.");
        }
        Chamado c = new Chamado(solicitante, req.titulo(), req.descricao(), req.prioridade());
        c.comentar(solicitante, "Chamado criado por " + solicitante.getNome());
        c.alterarStatus(StatusChamado.ABERTO, solicitante, "Status inicial: ABERTO");
        return chamadoRepo.save(c);
    }

    @Transactional
    public Chamado assumir(UUID tecnicoId, UUID chamadoId) {
        Usuario tecnico = getUsuario(tecnicoId);
        if (!tecnico.getRoles().contains(Role.TECNICO) && !tecnico.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Somente TECNICO ou ADMIN podem assumir chamados.");
        }
        Chamado c = getChamado(chamadoId);
        c.atribuirTecnico(tecnico, tecnico);
        return c;
    }

    @Transactional
    public Chamado alterarStatus(UUID autorId, UUID chamadoId, StatusChangeRequest req) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);


        if (req.novoStatus() == StatusChamado.CONCLUIDO) {
            boolean pode = (c.getTecnicoResponsavel() != null && c.getTecnicoResponsavel().getId().equals(autor.getId()))
                    || autor.getRoles().contains(Role.ADMIN);
            if (!pode) throw new IllegalStateException("Apenas o técnico responsável ou ADMIN podem concluir.");
        }
        c.alterarStatus(req.novoStatus(), autor, req.detalhe());
        return c;
    }

    @Transactional
    public Chamado comentar(UUID autorId, UUID chamadoId, ComentarioRequest req) {
        Usuario autor = getUsuario(autorId);
        Chamado c = getChamado(chamadoId);
        c.comentar(autor, req.mensagem());
        return c;
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
    private Usuario getUsuario(UUID id) {
        return usuarioRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
    }

    private Chamado getChamado(UUID id) {
        return chamadoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Chamado não encontrado"));
    }
}
