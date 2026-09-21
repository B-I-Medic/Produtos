package com.medic.Web.config.auth;

import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioReactiveAuthenticationManagerTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldAuthenticateAnActiveUserWithTheCorrectPassword() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("senha", user.getSenha())).thenReturn(true);

        StepVerifier.create(new UsuarioReactiveAuthenticationManager(repository, passwordEncoder)
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), "senha")))
                .assertNext(authentication -> {
                    assertEquals(user, authentication.getPrincipal());
                    assertEquals("ADMIN", authentication.getAuthorities().iterator().next().getAuthority());
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectMissingCredentials() {

        StepVerifier.create(new UsuarioReactiveAuthenticationManager(repository, passwordEncoder)
                        .authenticate(new UsernamePasswordAuthenticationToken("email", null)))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldRejectUnknownInactiveAndIncorrectUsers() {

        when(repository.findByEmail("unknown")).thenReturn(Mono.empty());
        StepVerifier.create(manager().authenticate(token("unknown", "senha")))
                .expectError(BadCredentialsException.class).verify();

        UsuarioModel inactive = TestDataFactory.usuarioModel();
        inactive.setAtivo(false);
        when(repository.findByEmail(inactive.getEmail())).thenReturn(Mono.just(inactive));
        StepVerifier.create(manager().authenticate(token(inactive.getEmail(), "senha")))
                .expectError(DisabledException.class).verify();

        UsuarioModel wrongPassword = TestDataFactory.usuarioModel();
        when(repository.findByEmail(wrongPassword.getEmail())).thenReturn(Mono.just(wrongPassword));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        StepVerifier.create(manager().authenticate(token(wrongPassword.getEmail(), "senha")))
                .expectError(BadCredentialsException.class).verify();
    }

    private UsuarioReactiveAuthenticationManager manager() {

        return new UsuarioReactiveAuthenticationManager(repository, passwordEncoder);
    }

    private UsernamePasswordAuthenticationToken token(String email, String password) {

        return new UsernamePasswordAuthenticationToken(email, password);
    }
}
