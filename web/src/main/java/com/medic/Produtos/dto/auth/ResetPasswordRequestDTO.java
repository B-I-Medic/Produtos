package com.medic.Produtos.dto.auth;

public record ResetPasswordRequestDTO(

        String email,
        String code,
        String senha
) {
}
