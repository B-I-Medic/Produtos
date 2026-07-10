package com.medic.Web.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAcessDenied implements ServerAccessDeniedHandler {

    private final ErrorResponseWriter writer;

    public JwtAcessDenied(ErrorResponseWriter writer) {
        this.writer = writer;
    }

    @Override
    public @NonNull Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        return writer.write(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "Acesso negado",
                "Voce nao tem permissao para acessar este recurso",
                exchange
        );
    }
}
