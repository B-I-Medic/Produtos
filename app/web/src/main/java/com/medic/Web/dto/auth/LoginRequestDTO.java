package com.medic.Web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email não é válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
