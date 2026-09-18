package com.medic.Web.service.auth;

import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.support.TestDataFactory;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService service = new JwtService();

    @Test
    void shouldGenerateToken() {

        UsuarioModel user = TestDataFactory.usuarioModel();

        assertNotNull(service.generateToken(user));
    }

    @Test
    void shouldValidateGeneratedToken() {

        assertTrue(service.isValid(generatedToken()));
    }

    @Test
    void shouldReadTokenSubject() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        assertEquals(user.getEmail(), service.getSubject(service.generateToken(user)));
    }

    @Test
    void shouldReadTokenRoles() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        assertEquals(user.getRole().name(), service.getRoles(service.generateToken(user)).getFirst());
    }

    @Test
    void shouldReturnAnEmptyRoleListWhenTheClaimIsMissing() throws Exception {

        Field field = JwtService.class.getDeclaredField("algorithm");
        field.setAccessible(true);
        Algorithm algorithm = (Algorithm) field.get(service);
        String token = JWT.create()
                .withIssuer("produto-auth-api")
                .withSubject("user@example.com")
                .sign(algorithm);

        assertTrue(service.getRoles(token).isEmpty());
    }

    @Test
    void shouldReadTokenExpiration() {

        assertNotNull(service.getExpires_in(generatedToken()));
    }

    private String generatedToken() {

        return service.generateToken(TestDataFactory.usuarioModel());
    }

    @Test
    void shouldReturnFalseForInvalidToken() {

        assertFalse(service.isValid("token-invalido"));
    }
}
