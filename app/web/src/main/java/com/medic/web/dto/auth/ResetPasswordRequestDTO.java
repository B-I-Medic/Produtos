package com.medic.web.dto.auth;

public record ResetPasswordRequestDTO(

        String email,
        String code,
        String senha
) {
}
