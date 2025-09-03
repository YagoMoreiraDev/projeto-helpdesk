package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.Prioridade;
import com.cloud.yagodev.helpdesk.enums.StatusChamado;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChamadoResponse(
        UUID id,
        String titulo,
        String descricao,
        StatusChamado status,
        Prioridade prioridade,
        UUID solicitanteId,
        String solicitanteNome,
        UUID tecnicoId,
        String tecnicoNome,
        Instant createdAt,
        Instant closedAt,
        List<ChamadoEventoResponse> eventos
) {
}
