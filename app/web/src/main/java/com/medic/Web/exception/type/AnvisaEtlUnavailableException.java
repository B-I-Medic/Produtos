package com.medic.Web.exception.type;

public class AnvisaEtlUnavailableException extends RuntimeException {

    public AnvisaEtlUnavailableException() {
        super("O processamento da Anvisa esta indisponivel no momento.");
    }
}
