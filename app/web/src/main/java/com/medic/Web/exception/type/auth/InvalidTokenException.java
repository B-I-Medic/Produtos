package com.medic.Web.exception.type.auth;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Token invalido");
    }
}
