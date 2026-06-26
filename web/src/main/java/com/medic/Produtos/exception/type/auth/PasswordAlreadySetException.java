package com.medic.Produtos.exception.type.auth;

public class PasswordAlreadySetException extends RuntimeException {
    public PasswordAlreadySetException() {
        super("Senha ja definida");
    }
}
