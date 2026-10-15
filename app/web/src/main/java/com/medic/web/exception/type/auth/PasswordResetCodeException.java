package com.medic.web.exception.type.auth;

public class PasswordResetCodeException extends RuntimeException {
    public PasswordResetCodeException(String message) {

        super(message);
    }
}
