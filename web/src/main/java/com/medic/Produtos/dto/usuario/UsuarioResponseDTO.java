package com.medic.Produtos.dto.usuario;

import com.medic.Produtos.model.usuario.Role;

import java.util.UUID;

public record UsuarioResponseDTO(

        UUID id,
        String nome,
        String email,
        Role role,
        boolean ativo
) {
}
