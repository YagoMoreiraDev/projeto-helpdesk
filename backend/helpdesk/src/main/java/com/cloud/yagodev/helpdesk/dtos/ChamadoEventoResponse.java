package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import com.cloud.yagodev.helpdesk.enums.TipoEvento;

import java.time.Instant;
import java.util.UUID;

public record ChamadoEventoResponse(
        UUID id,
        Instant quando,
        TipoEvento tipo,
        String autorNome,
        String detalhe,
        StatusChamado de,
        StatusChamado para
) {
}
