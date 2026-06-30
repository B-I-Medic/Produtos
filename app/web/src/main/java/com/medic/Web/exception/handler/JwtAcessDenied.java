package com.medic.Web.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAcessDenied implements ServerAccessDeniedHandler {

    @Override
    public @NonNull Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {

        log.error("NÃO AUTORIZADO: {}", ex.getMessage());

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
