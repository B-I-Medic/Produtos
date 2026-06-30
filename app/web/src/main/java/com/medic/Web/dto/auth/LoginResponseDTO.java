package com.medic.Web.dto.auth;

import com.medic.Web.model.usuario.Role;

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
