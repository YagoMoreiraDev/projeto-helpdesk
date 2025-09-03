package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.UsuarioCreateRequest;
import com.cloud.yagodev.helpdesk.dtos.UsuarioResponse;
import com.cloud.yagodev.helpdesk.dtos.UsuarioUpdateRequest;
import com.cloud.yagodev.helpdesk.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioCreateRequest req) {
        return ResponseEntity.ok(usuarioService.criar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable UUID id,
                                                     @RequestBody @Valid UsuarioUpdateRequest req) {
        return ResponseEntity.ok(usuarioService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLogico(@PathVariable UUID id) {
        usuarioService.deletarLogico(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscar(id));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarAtivos() {
        return ResponseEntity.ok(usuarioService.listarAtivos());
    }
}
