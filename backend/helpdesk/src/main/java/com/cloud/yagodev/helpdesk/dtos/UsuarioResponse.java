package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.Role;

import java.util.Set;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        boolean ativo,
        Set<Role> roles
) {
}
