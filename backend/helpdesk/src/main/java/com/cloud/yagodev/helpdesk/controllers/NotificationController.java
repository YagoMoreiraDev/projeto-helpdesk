package com.cloud.yagodev.helpdesk.controllers;


import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.repositories.UsuarioRepository;
import com.cloud.yagodev.helpdesk.services.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    private final UsuarioRepository usuarios;

    public NotificationController(NotificationService n, UsuarioRepository u) {
        this.notifications = n;
        this.usuarios = u;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication auth) {
        UUID id = currentUserId(auth);
        Usuario u = usuarios.findById(id).orElseThrow();
        return notifications.subscribe(u);
    }

    private UUID currentUserId(Authentication auth) {
        return usuarios.findByEmailIgnoreCase(auth.getName())
                .map(Usuario::getId).orElseThrow();
    }
}
