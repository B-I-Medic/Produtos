package com.medic.Web.dto.usuario;

import com.medic.Web.model.usuario.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email está inválido")
        String email,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "A role é obrigatória")
        Role role
) {
}
