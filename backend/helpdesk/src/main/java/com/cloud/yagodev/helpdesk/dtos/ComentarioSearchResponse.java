package com.cloud.yagodev.helpdesk.dtos;

import java.time.Instant;
import java.util.UUID;

public record ComentarioSearchResponse(
        UUID eventoId,
        Instant quando,
        String detalhe,
        UUID chamadoId,
        String chamadoTitulo,
        UUID autorId,
        String autorNome
) {}