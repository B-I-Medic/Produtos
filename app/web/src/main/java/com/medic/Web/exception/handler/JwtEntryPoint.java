package com.medic.Web.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtEntryPoint implements ServerAuthenticationEntryPoint {

    private final ErrorResponseWriter writer;

    public JwtEntryPoint(ErrorResponseWriter writer) {
        this.writer = writer;
    }

    @Override
    public @NonNull Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return writer.write(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Autenticacao",
                ex.getMessage() == null ? "Token invalido" : ex.getMessage(),
                exchange
        );
    }
}
