package com.medic.Produtos.exception.type;

public class NotFounException extends RuntimeException {
    public NotFounException(String entity, String identiy, String field) {

        super(
                String.format("O(A) %s (%s = %s) não foi encontrado.", entity, field, identiy)
        );
    }
}
