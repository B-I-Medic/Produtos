package com.medic.Web.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(

        @NotBlank(message = "O refresh token e obrigatorio")
        String refreshToken
) {
}
