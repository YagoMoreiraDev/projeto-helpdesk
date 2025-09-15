package com.cloud.yagodev.helpdesk.controllers;

import com.cloud.yagodev.helpdesk.dtos.GenericMessage;
import com.cloud.yagodev.helpdesk.dtos.FirstAccessStartRequest;
import com.cloud.yagodev.helpdesk.dtos.FirstAccessCompleteRequest;
import com.cloud.yagodev.helpdesk.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/first-access")
public class AuthFirstAccessController {

    private final UsuarioService usuarioService;

    public AuthFirstAccessController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Etapa 1: envia token (por e-mail / SMS / etc.)
    @PostMapping("/start")
    public ResponseEntity<GenericMessage> start(@RequestBody @Valid FirstAccessStartRequest req) {
        return ResponseEntity.ok(usuarioService.startFirstAccess(req));
    }

    // Etapa 2: confirma token e define senha
    @PostMapping("/complete")
    public ResponseEntity<GenericMessage> complete(@RequestBody @Valid FirstAccessCompleteRequest req) {
        return ResponseEntity.ok(usuarioService.completeFirstAccess(req));
    }
}
