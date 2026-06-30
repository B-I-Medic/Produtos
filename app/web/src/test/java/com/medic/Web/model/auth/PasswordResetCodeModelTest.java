package com.medic.Web.model.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetCodeModelTest {

    @Test
    void shouldMarkPasswordResetCodeAsUsed() {

        PasswordResetCodeModel model = new PasswordResetCodeModel();

        model.marcarComoUsado();

        assertTrue(model.isUsado());
    }
}
