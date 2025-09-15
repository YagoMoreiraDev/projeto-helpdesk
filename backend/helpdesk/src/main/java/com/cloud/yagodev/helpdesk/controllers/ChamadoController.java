package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.*;
import com.cloud.yagodev.helpdesk.entities.Chamado;
import com.cloud.yagodev.helpdesk.entities.ChamadoEvento;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import com.cloud.yagodev.helpdesk.services.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final UsuarioRepository usuarioRepo;

    public ChamadoController(ChamadoService chamadoService, UsuarioRepository usuarioRepo) {
        this.chamadoService = chamadoService;
        this.usuarioRepo = usuarioRepo;
    }

    // --- criar chamado: solicitante vem do token ---
    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(@RequestBody @Valid ChamadoCreateRequest req,
                                                 Authentication auth) {
        UUID solicitanteId = currentUserId(auth);
        Chamado c = chamadoService.abrirChamado(solicitanteId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(c));
    }

    // --- alterar status: autor vem do token ---
    @PostMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> alterarStatus(@PathVariable("id") UUID chamadoId,
                                                         @RequestBody @Valid StatusChangeRequest req,
                                                         Authentication auth) {
        UUID autorId = currentUserId(auth);
        Chamado c = chamadoService.alterarStatus(autorId, chamadoId, req);
        return ResponseEntity.ok(toResponse(c));
    }

    // --- técnico assume: técnico vem do token; restrito a TECNICO/ADMIN ---
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @PostMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponse> assumir(@PathVariable("id") UUID chamadoId,
                                                   Authentication auth) {
        UUID tecnicoId = currentUserId(auth);
        Chamado c = chamadoService.assumir(tecnicoId, chamadoId);
        return ResponseEntity.ok(toResponse(c));
    }

    // --- comentar: autor vem do token ---
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ChamadoResponse> comentar(@PathVariable("id") UUID chamadoId,
                                                    @RequestBody @Valid ComentarioRequest req,
                                                    Authentication auth) {
        UUID autorId = currentUserId(auth);
        Chamado c = chamadoService.comentar(autorId, chamadoId, req);
        return ResponseEntity.ok(toResponse(c));
    }

    // --- listagens: sem query param; usa usuário logado ---
    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponse>> meus(Authentication auth) {
        UUID solicitanteId = currentUserId(auth);
        return ResponseEntity.ok(chamadoService.listarMeusChamados(solicitanteId)
                .stream().map(this::toResponse).toList());
    }

    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @GetMapping("/tecnico")
    public ResponseEntity<List<ChamadoResponse>> porTecnico(Authentication auth) {
        UUID tecnicoId = currentUserId(auth);
        return ResponseEntity.ok(chamadoService.listarPorTecnico(tecnicoId)
                .stream().map(this::toResponse).toList());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')") // opcional: só admin lista tudo
    public ResponseEntity<List<ChamadoResponse>> todos() {
        return ResponseEntity.ok(chamadoService.listarTodos()
                .stream().map(this::toResponse).toList());
    }

    // listar somente EM_ABERTO (técnico precisa ver todos os abertos)
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    @GetMapping("/abertos")
    public ResponseEntity<List<ChamadoResponse>> abertos() {
        return ResponseEntity.ok(
                chamadoService.listarEmAberto().stream().map(this::toResponse).toList()
        );
    }

    // excluir / cancelar chamado (só o solicitante dono ou ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id, Authentication auth) {
        UUID autorId = currentUserId(auth);
        chamadoService.excluirChamado(autorId, id); // implemente regra: só se for dono e, por ex., EM_ABERTO
        return ResponseEntity.noContent().build();
    }

    // ADMIN designa técnico específico
    @PostMapping("/{id}/designar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChamadoResponse> designar(@PathVariable("id") UUID chamadoId,
                                                    @RequestParam UUID tecnicoId,
                                                    Authentication auth) {
        UUID adminId = currentUserId(auth);
        Chamado c = chamadoService.designarTecnico(adminId, chamadoId, tecnicoId);
        return ResponseEntity.ok(toResponse(c));
    }

    // Cancelar (em vez de DELETE físico)
    public record CancelamentoRequest(String detalhe) {}
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ChamadoResponse> cancelar(@PathVariable UUID id,
                                                    @RequestBody(required = false) CancelamentoRequest req,
                                                    Authentication auth) {
        UUID autorId = currentUserId(auth);
        Chamado c = chamadoService.cancelarChamado(autorId, id, req != null ? req.detalhe() : null);
        return ResponseEntity.ok(toResponse(c));
    }

    // Chamados sem técnico (abertos e sem responsável) – técnico e admin enxergam
    @GetMapping("/sem-tecnico")
    @PreAuthorize("hasRole('TECNICO') or hasRole('ADMIN')")
    public ResponseEntity<List<ChamadoResponse>> semTecnico() {
        return ResponseEntity.ok(
                chamadoService.listarSemTecnicoEmAberto().stream().map(this::toResponse).toList()
        );
    }

    // Filtro por status para painéis do ADMIN
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChamadoResponse>> porStatus(@RequestParam StatusChamado status) {
        return ResponseEntity.ok(
                chamadoService.listarPorStatus(status).stream().map(this::toResponse).toList()
        );
    }

    @GetMapping("/ping")
    public Map<String, Long> ping() {
        return Map.of("lastUpdate", chamadoService.getLastUpdate());
    }

    // ---- helpers ----
    private UUID currentUserId(Authentication auth) {
        // O JwtAuthenticationFilter setou principal como e-mail; buscamos o usuário
        String email = auth.getName();
        return usuarioRepo.findByEmailIgnoreCase(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    /** Retorna o “código curto” (6 últimos hex do UUID) de um chamado específico. */
    @GetMapping("/{id}/codigo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO') or hasRole('USUARIO_COMUM')")
    public ResponseEntity<ChamadoCodigoDto> codigoDeChamado(@PathVariable("id") UUID chamadoId) {
        return ResponseEntity.ok(chamadoService.codigoDeChamado(chamadoId));
    }

    /** Lista {id, codigo} de todos os chamados (útil p/ relatórios; protegi para ADMIN). */
    @GetMapping("/codigos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChamadoCodigoDto>> listarCodigosCurtos() {
        return ResponseEntity.ok(chamadoService.listarCodigosCurtos());
    }

    /** Busca por código curto. Pode retornar 0, 1 ou N chamados (se colidir). */
    @GetMapping("/por-codigo/{codigo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public ResponseEntity<List<ChamadoResponse>> buscarPorCodigo(@PathVariable("codigo") String codigo) {
        var list = chamadoService.buscarPorCodigoCurto(codigo);
        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

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
            var m = ev.getClass().getMethod("getDetalhe");
            return (String) m.invoke(ev);
        } catch (Exception ignore) {
            try {
                var m2 = ev.getClass().getMethod("getDetallhe");
                return (String) m2.invoke(ev);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
