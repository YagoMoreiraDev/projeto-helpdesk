package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.StatusChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull StatusChamado novoStatus,
        @NotBlank String detalhe
) {
}
