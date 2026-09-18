package com.medic.Web.dto.auth;

import com.medic.Web.model.usuario.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record LoginResponseDTO(

        String nome,
        String email,
        Role role,
        boolean primeiroAcesso,
        String token,
        Instant expires_in,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("refresh_expires_in") Instant refreshExpiresIn
) {
}
