package com.medic.Web.support;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class FixedAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

    private final Object principal;

    public FixedAuthenticationPrincipalResolver(Object principal) {
        this.principal = principal;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter,
                                                    BindingContext bindingContext,
                                                    ServerWebExchange exchange) {

        return Mono.just(principal);
    }
}
