package com.medic.Web.service.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetCodeGeneratorTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetCodeGenerator service;

    @Test
    void shouldGenerateSixDigitCode() {

        assertEquals(6, service.generate().length());
    }

    @Test
    void shouldHashCode() {

        when(passwordEncoder.encode("123456")).thenReturn("hash");

        assertEquals("hash", service.hash("123456"));
    }

    @Test
    void shouldValidateCode() {

        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);

        assertTrue(service.valide("123456", "hash"));
    }
}
