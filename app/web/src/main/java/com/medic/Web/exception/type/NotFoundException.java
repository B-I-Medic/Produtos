package com.medic.Web.exception.type;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, String identity, String field) {

        super(
                String.format("O(A) %s (%s = %s) não foi encontrado.", entity, field, identity)
        );
    }
}
