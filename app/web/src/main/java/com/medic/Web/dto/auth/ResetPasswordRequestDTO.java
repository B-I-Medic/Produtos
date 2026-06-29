package com.medic.Web.dto.auth;

public record ResetPasswordRequestDTO(

        String email,
        String code,
        String senha
) {
}
