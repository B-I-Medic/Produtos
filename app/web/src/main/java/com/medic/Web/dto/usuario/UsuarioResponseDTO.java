package com.medic.Web.dto.usuario;

import com.medic.Web.model.usuario.Role;

import java.util.UUID;

public record UsuarioResponseDTO(

        UUID id,
        String nome,
        String email,
        Role role,
        boolean ativo
) {
}
