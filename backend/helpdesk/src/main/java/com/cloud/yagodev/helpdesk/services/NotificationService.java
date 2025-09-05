package com.cloud.yagodev.helpdesk.services;

import com.cloud.yagodev.helpdesk.dtos.NotificationDto;
import com.cloud.yagodev.helpdesk.entities.Usuario;
import com.cloud.yagodev.helpdesk.enums.Role;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    static final long SSE_TIMEOUT = 0L; // sem timeout (mantemos com ping)
    private record Client(UUID userId, Set<Role> roles, SseEmitter emitter) {}

    private final Map<UUID, Client> clients = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Usuario u) {
        var emitter = new SseEmitter(SSE_TIMEOUT);
        var client = new Client(u.getId(), u.getRoles(), emitter);
        clients.put(u.getId(), client);

        emitter.onCompletion(() -> clients.remove(u.getId()));
        emitter.onTimeout(() -> clients.remove(u.getId()));
        emitter.onError(ex -> clients.remove(u.getId()));

        // primeira mensagem opcional
        safeSend(client, "CONNECTED", Map.of("userId", u.getId().toString()));
        return emitter;
    }

    public void sendToUser(UUID userId, NotificationDto dto) {
        var c = clients.get(userId);
        if (c != null) safeSend(c, dto);
    }

    public void sendToRole(Role role, NotificationDto dto) {
        clients.values().forEach(c -> {
            if (c.roles().contains(role)) safeSend(c, dto);
        });
    }

    private void safeSend(Client c, NotificationDto dto) {
        try {
            c.emitter().send(SseEmitter.event()
                    .name(dto.type().name())
                    .data(dto));
        } catch (IOException ignored) {
            clients.remove(c.userId());
        }
    }
    private void safeSend(Client c, String name, Object payload) {
        try {
            c.emitter().send(SseEmitter.event().name(name).data(payload));
        } catch (IOException ignored) {
            clients.remove(c.userId());
        }
    }

    // Mantém a conexão viva (e detecta clientes mortos)
    @Scheduled(fixedRate = 15000)
    public void ping() {
        clients.values().forEach(c -> safeSend(c, "PING", Map.of("ts", System.currentTimeMillis())));
    }
}
