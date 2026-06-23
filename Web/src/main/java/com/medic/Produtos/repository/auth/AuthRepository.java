package com.medic.Produtos.repository.auth;

import com.medic.Produtos.repository.usuario.UsuarioRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class AuthRepository implements ReactiveUserDetailsService {

    private final UsuarioRepository repository;

    public AuthRepository(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public @NullMarked Mono<UserDetails> findByUsername(String username) {

        return repository.findByEmail(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)))
                .map(user -> User
                        .withUsername(user.getEmail())
                        .password(user.getSenha())
                        .roles(user.getRole().name())
                        .build()
                );
    }
}
