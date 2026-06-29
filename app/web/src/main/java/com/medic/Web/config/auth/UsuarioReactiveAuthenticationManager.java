package com.medic.Web.config.auth;

import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Primary
@Component
public class UsuarioReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioReactiveAuthenticationManager(UsuarioRepository repository,
                                                PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public @NonNull Mono<Authentication> authenticate(Authentication authentication) {

        String email = authentication.getName();
        Object credentials = authentication.getCredentials();

        if (credentials == null) {
            return Mono.error(new BadCredentialsException("Credenciais invalidas"));
        }

        String senha = credentials.toString();

        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Email inválido")))
                .filter(UsuarioModel::getAtivo)
                .switchIfEmpty(Mono.error(new DisabledException("Usuario inativo")))
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Senha inválida")))
                .map(this::toAuthentication);
    }

    private Authentication toAuthentication(UsuarioModel usuario) {

        var authorities = List.of(new SimpleGrantedAuthority(usuario.getRole().name()));

        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                authorities
        );
    }
}
