package com.medic.web.dto.usuario;

import com.medic.web.model.usuario.Role;

import java.util.UUID;

public record UsuarioResponseDTO(

        UUID id,
        String nome,
        String email,
        Role role,
        boolean ativo
) {
}
