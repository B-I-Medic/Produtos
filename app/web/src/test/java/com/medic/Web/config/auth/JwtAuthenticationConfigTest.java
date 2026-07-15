package com.medic.Web.config.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.medic.Web.exception.handler.ErrorResponseWriter;
import com.medic.Web.exception.handler.JwtEntryPoint;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationConfigTest {

    @Test
    void shouldReturnUnauthorizedForInvalidBearerToken() {
        JwtService jwtService = mock(JwtService.class);
        UsuarioRepository repository = mock(UsuarioRepository.class);
        JwtEntryPoint entryPoint = new JwtEntryPoint(new ErrorResponseWriter());
        JwtAuthenticationConfig filter = new JwtAuthenticationConfig(jwtService, repository, entryPoint);
        WebFilterChain chain = mock(WebFilterChain.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/rabbitmq-test/teste?message=aa")
                        .header("Authorization", "Bearer aaa")
                        .build()
        );

        when(jwtService.getSubject("aaa")).thenThrow(new JWTVerificationException("invalid"));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertTrue(exchange.getResponse().getBodyAsString().block().contains("\"descricao\":\"Token inválido\""));
    }
}
