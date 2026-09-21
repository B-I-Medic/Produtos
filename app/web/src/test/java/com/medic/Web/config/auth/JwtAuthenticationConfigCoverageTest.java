package com.medic.Web.config.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.medic.Web.exception.handler.JwtEntryPoint;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.auth.JwtService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationConfigCoverageTest {

    @Test
    void shouldBypassRefreshLogoutAndRequestsWithoutBearerToken() {

        JwtService jwtService = mock(JwtService.class);
        UsuarioRepository repository = mock(UsuarioRepository.class);
        JwtAuthenticationConfig filter = filter(jwtService, repository, mock(JwtEntryPoint.class));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        for (String path : new String[]{"/auth/refresh", "/auth/logout", "/public"}) {
            var request = MockServerHttpRequest.get(path).build();
            StepVerifier.create(filter.filter(MockServerWebExchange.from(request), chain)).verifyComplete();
        }

        verify(chain, org.mockito.Mockito.times(3)).filter(any());
    }

    @Test
    void shouldAuthenticateAValidUserAndMapJwtRoles() {

        JwtService jwtService = mock(JwtService.class);
        UsuarioRepository repository = mock(UsuarioRepository.class);
        JwtEntryPoint entryPoint = mock(JwtEntryPoint.class);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        UsuarioModel user = TestDataFactory.usuarioModel();
        when(jwtService.getSubject("token")).thenReturn(user.getEmail());
        when(jwtService.getRoles("token")).thenReturn(java.util.List.of("ADMIN"));
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));

        StepVerifier.create(filter(jwtService, repository, entryPoint).filter(
                        MockServerWebExchange.from(MockServerHttpRequest.get("/secure")
                                .header("Authorization", "Bearer token").build()), chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void shouldRejectUnknownAndInactiveUsers() {

        JwtService jwtService = mock(JwtService.class);
        UsuarioRepository repository = mock(UsuarioRepository.class);
        JwtEntryPoint entryPoint = mock(JwtEntryPoint.class);
        when(jwtService.getSubject("token")).thenReturn("unknown");
        when(jwtService.getRoles("token")).thenReturn(java.util.List.of("USER"));
        when(repository.findByEmail("unknown")).thenReturn(Mono.empty());
        when(entryPoint.commence(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(filter(jwtService, repository, entryPoint).filter(
                        MockServerWebExchange.from(MockServerHttpRequest.get("/secure")
                                .header("Authorization", "Bearer token").build()), mock(WebFilterChain.class)))
                .expectError(BadCredentialsException.class)
                .verify();

        UsuarioModel inactive = TestDataFactory.usuarioModel();
        inactive.setAtivo(false);
        when(jwtService.getSubject("token2")).thenReturn(inactive.getEmail());
        when(jwtService.getRoles("token2")).thenReturn(java.util.List.of("USER"));
        when(repository.findByEmail(inactive.getEmail())).thenReturn(Mono.just(inactive));

        StepVerifier.create(filter(jwtService, repository, entryPoint).filter(
                        MockServerWebExchange.from(MockServerHttpRequest.get("/secure")
                                .header("Authorization", "Bearer token2").build()), mock(WebFilterChain.class)))
                .expectError(DisabledException.class)
                .verify();
    }

    @Test
    void shouldUseTheEntryPointForExpiredAndInvalidTokens() {

        JwtService jwtService = mock(JwtService.class);
        UsuarioRepository repository = mock(UsuarioRepository.class);
        JwtEntryPoint entryPoint = mock(JwtEntryPoint.class);
        when(entryPoint.commence(any(), any())).thenReturn(Mono.empty());
        when(jwtService.getSubject("expired")).thenThrow(new TokenExpiredException("expired", Instant.now()));
        when(jwtService.getSubject("invalid")).thenThrow(new JWTVerificationException("invalid"));
        var filter = filter(jwtService, repository, entryPoint);

        StepVerifier.create(filter.filter(exchange("expired"), mock(WebFilterChain.class))).verifyComplete();
        StepVerifier.create(filter.filter(exchange("invalid"), mock(WebFilterChain.class))).verifyComplete();
        verify(entryPoint, org.mockito.Mockito.times(2)).commence(any(), any());
    }

    private JwtAuthenticationConfig filter(JwtService jwtService,
                                           UsuarioRepository repository,
                                           JwtEntryPoint entryPoint) {

        return new JwtAuthenticationConfig(jwtService, repository, entryPoint);
    }

    private MockServerWebExchange exchange(String token) {

        return MockServerWebExchange.from(MockServerHttpRequest.get("/secure")
                .header("Authorization", "Bearer " + token).build());
    }
}
