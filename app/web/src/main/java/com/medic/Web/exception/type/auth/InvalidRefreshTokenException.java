package com.medic.Web.exception.type.auth;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException() {
        super("Refresh token invalido ou expirado");
    }
}
