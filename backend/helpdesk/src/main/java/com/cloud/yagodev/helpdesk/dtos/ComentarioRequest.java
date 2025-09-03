package com.cloud.yagodev.helpdesk.dtos;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(
        @NotBlank
        String mensagem
) {
}
