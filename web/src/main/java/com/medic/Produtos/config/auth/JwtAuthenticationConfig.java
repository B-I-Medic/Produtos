package com.medic.Produtos.config.auth;

import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.repository.usuario.UsuarioRepository;
import com.medic.Produtos.service.auth.JwtService;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class JwtAuthenticationConfig implements WebFilter {

    private final JwtService jwtService;
    private final UsuarioRepository repository;

    public JwtAuthenticationConfig(JwtService jwtService, UsuarioRepository repository) {
        this.jwtService = jwtService;
        this.repository = repository;
    }

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {

        String header = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = header.substring(7);

        if (!jwtService.isValid(token)) {
            return chain.filter(exchange);
        }

        String subject = jwtService.getSubject(token);

        List<SimpleGrantedAuthority> authorities = jwtService.getRoles(token)
                .stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();

        return repository.findByEmail(subject)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Email inválido")))
                .filter(UsuarioModel::getAtivo)
                .switchIfEmpty(Mono.error(new DisabledException("Usuario inativo")))
                .map(u -> new UsernamePasswordAuthenticationToken(u, null, authorities))
                .flatMap(auth -> 
                    chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                );
    }
}
