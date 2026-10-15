package com.medic.web.exception.type.auth;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException() {
        super("Token inválido");
    }
}
