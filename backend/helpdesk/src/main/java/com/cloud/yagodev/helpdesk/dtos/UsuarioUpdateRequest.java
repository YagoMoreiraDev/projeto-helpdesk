package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UsuarioUpdateRequest(
        @NotBlank
        String nome,
        @Email
        @NotBlank
        String email,
        @NotNull
        Set<Role> roles
) {
}
