package com.medic.Produtos.dto.auth;

import com.medic.Produtos.model.usuario.Role;

import java.time.Instant;

public record LoginResponseDTO(

        String nome,
        String email,
        Role role,
        boolean primeiroAcesso,
        String token,
        Instant expires_in
) {
}
