package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChamadoCreateRequest(
        @NotBlank String titulo,
        @NotBlank String descricao,
        @NotNull Prioridade prioridade
) {
}
