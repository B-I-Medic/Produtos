package com.medic.Web.exception.handler;

import com.medic.Web.exception.type.auth.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtHandlersTest {

    private final ErrorResponseWriter writer = new ErrorResponseWriter();
    private final JwtEntryPoint entryPoint = new JwtEntryPoint(writer);
    private final JwtAcessDenied accessDenied = new JwtAcessDenied(writer);

    @Test
    void shouldWriteJwtUnauthorizedBody() {
        var exchange = exchange("/auth/login");

        StepVerifier.create(entryPoint.commence(exchange, new BadCredentialsException("Token expirado")))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertTrue(readBody(exchange).contains("\"erro\":\"Autenticacao\""));
        assertTrue(readBody(exchange).contains("\"descricao\":\"Token expirado\""));
        assertTrue(readBody(exchange).contains("\"path\":\"/auth/login\""));
    }

    @Test
    void shouldWriteJwtUnauthorizedBodyForInvalidToken() {
        var exchange = exchange("/swagger-ui/index.html");

        StepVerifier.create(entryPoint.commence(exchange, new InvalidTokenException()))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertTrue(readBody(exchange).contains("\"erro\":\"Autenticacao\""));
        assertTrue(readBody(exchange).contains("\"descricao\":\"Token inválido\""));
        assertTrue(readBody(exchange).contains("\"path\":\"/swagger-ui/index.html\""));
    }

    @Test
    void shouldWriteJwtForbiddenBody() {
        var exchange = exchange("/usuario/save");

        StepVerifier.create(accessDenied.handle(exchange, new AccessDeniedException("negado")))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        assertTrue(readBody(exchange).contains("\"erro\":\"Acesso negado\""));
        assertTrue(readBody(exchange).contains("\"descricao\":\"Voce nao tem permissao para acessar este recurso\""));
        assertTrue(readBody(exchange).contains("\"path\":\"/usuario/save\""));
    }

    private static MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private static String readBody(MockServerWebExchange exchange) {
        return exchange.getResponse().getBodyAsString().block();
    }
}
