package com.medic.Web.exception.type.auth;

public class PasswordResetCodeException extends RuntimeException {
    public PasswordResetCodeException(String message) {

        super(message);
    }
}
