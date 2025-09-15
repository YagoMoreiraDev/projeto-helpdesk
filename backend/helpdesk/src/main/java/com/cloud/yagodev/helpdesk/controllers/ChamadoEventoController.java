package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.ComentarioSearchResponse;
import com.cloud.yagodev.helpdesk.dtos.PageResult;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.Role;
import com.cloud.yagodev.helpdesk.repositories.ChamadoEventoRepository;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chamados/eventos")
public class ChamadoEventoController {

    private final ChamadoEventoRepository eventosRepo;
    private final UsuarioRepository usuarioRepo;

    public ChamadoEventoController(ChamadoEventoRepository eventosRepo, UsuarioRepository usuarioRepo) {
        this.eventosRepo = eventosRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping("/comentarios/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO') or hasRole('USUARIO_COMUM')")
    public ResponseEntity<PageResult<ComentarioSearchResponse>> searchComentarios(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication auth
    ) {
        String query = (q == null ? "" : q.trim());
        if (query.length() < 2) {
            // evita varrer a tabela inteira sem necessidade
            return ResponseEntity.badRequest().build();
        }

        var user = currentUser(auth);
        var pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));

        Page<ComentarioSearchResponse> result;
        if (user.hasRole(Role.ADMIN)) {
            result = eventosRepo.searchComentariosAll(query, pageable);
        } else if (user.hasRole(Role.TECNICO)) {
            result = eventosRepo.searchComentariosDoTecnico(user.getId(), query, pageable);
        } else {
            // USUARIO_COMUM
            result = eventosRepo.searchComentariosDoSolicitante(user.getId(), query, pageable);
        }

        var body = new PageResult<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(body);
    }

    // ---- helpers ----
    private Usuario currentUser(Authentication auth) {
        String email = auth.getName();
        return usuarioRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
