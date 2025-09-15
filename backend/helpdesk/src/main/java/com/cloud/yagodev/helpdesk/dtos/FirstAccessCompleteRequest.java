package com.cloud.yagodev.helpdesk.dtos;

import jakarta.validation.constraints.Size;

public record FirstAccessCompleteRequest(
        String email,
        String token,
        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")String novaSenha) {
}
