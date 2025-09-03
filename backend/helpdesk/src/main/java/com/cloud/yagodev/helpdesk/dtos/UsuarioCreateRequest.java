package com.cloud.yagodev.helpdesk.dtos;

import com.cloud.yagodev.helpdesk.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.Set;


public record UsuarioCreateRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") String senha,
        @NotNull Set<Role> roles
){}
