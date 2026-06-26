package com.medic.Produtos.service.auth;

import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService service = new JwtService();

    @Test
    void shouldGenerateAndReadToken() {

        UsuarioModel user = TestDataFactory.usuarioModel();

        String token = service.generateToken(user);

        assertTrue(service.isValid(token));
        assertEquals(user.getEmail(), service.getSubject(token));
        assertEquals(user.getRole().name(), service.getRoles(token).getFirst());
        assertNotNull(service.getExpires_in(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {

        assertFalse(service.isValid("token-invalido"));
    }
}
