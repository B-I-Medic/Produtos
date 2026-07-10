package com.medic.Web.config.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.auth.JwtService;
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

        try {
            String subject = jwtService.getSubject(token);

            List<SimpleGrantedAuthority> authorities = jwtService.getRoles(token)
                    .stream()
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return repository.findByEmail(subject)
                    .switchIfEmpty(Mono.error(new BadCredentialsException("Email invalido")))
                    .filter(UsuarioModel::getAtivo)
                    .switchIfEmpty(Mono.error(new DisabledException("Usuario inativo")))
                    .map(u -> new UsernamePasswordAuthenticationToken(u, null, authorities))
                    .flatMap(auth ->
                            chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                    );
        } catch (TokenExpiredException ex) {
            return Mono.error(new BadCredentialsException("Token expirado"));
        } catch (JWTVerificationException ex) {
            return Mono.error(new BadCredentialsException("Token invalido"));
        }
    }
}
