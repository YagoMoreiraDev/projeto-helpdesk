package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        NotificationType type,
        String message,
        UUID chamadoId,
        Instant createdAt,
        ChamadoResponse chamado // pode vir nulo
) {
    public static NotificationDto of(NotificationType t, String msg, ChamadoResponse c) {
        return new NotificationDto(t, msg, c != null ? c.id() : null, Instant.now(), c);
    }
}
