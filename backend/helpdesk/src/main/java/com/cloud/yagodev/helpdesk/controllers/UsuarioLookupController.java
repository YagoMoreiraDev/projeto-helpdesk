package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioLookupController {
    private final UsuarioRepository repo;
    public UsuarioLookupController(UsuarioRepository repo) { this.repo = repo; }

    public record UsuarioLookup(UUID id, String nome, String email) {}

    @GetMapping("/tecnicos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioLookup> tecnicos() {
        return repo.findAll().stream()
                .filter(u -> u.getRoles().contains(com.cloud.yagodev.helpdesk.enums.Role.TECNICO))
                .map(u -> new UsuarioLookup(u.getId(), u.getNome(), u.getEmail()))
                .toList();
    }
}
