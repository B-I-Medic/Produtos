package com.medic.Web.service.auth;

import com.medic.Web.dto.auth.LoginRequestDTO;
import com.medic.Web.dto.auth.PasswordRequestDTO;
import com.medic.Web.dto.auth.ResetPasswordRequestDTO;
import com.medic.Web.mapper.auth.PasswordResetCodeMapper;
import com.medic.Web.model.auth.PasswordResetCodeModel;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.auth.PasswordResetCodeRepository;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.service.mail.MailService;
import com.medic.Web.service.mail.MailTemplateService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ReactiveAuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UsuarioRepository repository;
    @Mock
    private MailService mailService;
    @Mock
    private MailTemplateService mailTemplateService;
    @Mock
    private PasswordResetCodeGenerator passwordResetCodeGenerator;
    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;
    @Mock
    private PasswordResetCodeMapper passwordResetCodeMapper;

    @InjectMocks
    private AuthService service;

    @Test
    void shouldLogin() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        when(authenticationManager.authenticate(any()))
                .thenReturn(Mono.just(new TestingAuthenticationToken(user, null)));
        when(refreshTokenService.issue(user))
                .thenReturn(Mono.just(new RefreshTokenService.IssuedRefreshToken(
                        java.util.UUID.randomUUID(), "refresh", Instant.now().plusSeconds(3600))));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.getExpires_in("token")).thenReturn(Instant.now().plusSeconds(3600));

        StepVerifier.create(service.login(new LoginRequestDTO(user.getEmail(), "123")))
                .expectNextMatches(response -> response.email().equals(user.getEmail())
                        && response.token().equals("token")
                        && response.refreshToken().equals("refresh"))
                .verifyComplete();
    }

    @Test
    void shouldRefreshAndLogoutThroughTheRefreshTokenService() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        Instant expiresAt = Instant.now().plusSeconds(3600);
        when(refreshTokenService.rotate("refresh-old"))
                .thenReturn(Mono.just(new RefreshTokenService.RotatedRefreshToken(user, "refresh-new", expiresAt)));
        when(jwtService.generateToken(user)).thenReturn("new-jwt");
        when(jwtService.getExpires_in("new-jwt")).thenReturn(expiresAt);

        StepVerifier.create(service.refresh("refresh-old"))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("refresh-new", response.refreshToken());
                    org.junit.jupiter.api.Assertions.assertEquals("new-jwt", response.token());
                })
                .verifyComplete();

        when(refreshTokenService.revoke("refresh-new")).thenReturn(Mono.empty());
        StepVerifier.create(service.logout("refresh-new")).verifyComplete();
    }

    @Test
    void shouldFirstAccess() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("nova")).thenReturn("hash");
        when(repository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(service.firstAccess(new PasswordRequestDTO("nova"), user.getId()))
                .verifyComplete();
    }

    @Test
    void shouldForgotPassword() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        PasswordResetCodeModel code = TestDataFactory.passwordResetCodeModel();
        when(passwordResetCodeGenerator.generate()).thenReturn("123456");
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordResetCodeGenerator.hash("123456")).thenReturn("hash-code");
        when(passwordResetCodeMapper.toEntity(anyString(), anyString(), any())).thenReturn(code);
        when(passwordResetCodeRepository.save(code)).thenReturn(Mono.just(code));
        when(mailTemplateService.forgotPassword(user.getNome(), "123456")).thenReturn(Mono.just("body"));
        when(mailService.sendSimpleEmail(org.mockito.ArgumentMatchers.eq(user.getEmail()), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq("body")))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.forgotPassword(user.getEmail()))
                .verifyComplete();
    }

    @Test
    void shouldResetPassword() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        PasswordResetCodeModel code = TestDataFactory.passwordResetCodeModel();
        when(passwordResetCodeRepository.findFirstByEmailAndUsadoFalseAndExpiraEmAfterOrderByExpiraEmDesc(anyString(), any()))
                .thenReturn(Mono.just(code));
        when(passwordResetCodeGenerator.valide("123456", code.getCodigo())).thenReturn(true);
        when(passwordResetCodeRepository.save(code)).thenReturn(Mono.just(code));
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("nova")).thenReturn("hash");
        when(repository.save(user)).thenReturn(Mono.just(user));
        when(refreshTokenService.revokeAllForUser(user.getId())).thenReturn(Mono.empty());

        StepVerifier.create(service.resetPassword(new ResetPasswordRequestDTO(user.getEmail(), "123456", "nova")))
                .verifyComplete();
    }

    @Test
    void shouldRejectFirstAccessForInactiveOrAlreadyInitializedUsers() {

        UsuarioModel inactive = TestDataFactory.usuarioModel();
        inactive.setAtivo(false);
        when(repository.findById(inactive.getId())).thenReturn(Mono.just(inactive));

        StepVerifier.create(service.firstAccess(new PasswordRequestDTO("nova"), inactive.getId()))
                .expectError(org.springframework.security.authentication.DisabledException.class)
                .verify();

        UsuarioModel initialized = TestDataFactory.usuarioModel();
        initialized.setPrimeiroAcesso(false);
        when(repository.findById(initialized.getId())).thenReturn(Mono.just(initialized));

        StepVerifier.create(service.firstAccess(new PasswordRequestDTO("nova"), initialized.getId()))
                .expectError(com.medic.Web.exception.type.auth.PasswordAlreadySetException.class)
                .verify();
    }

    @Test
    void shouldRejectForgotPasswordForMissingOrInactiveUsers() {

        when(repository.findByEmail("missing@example.com")).thenReturn(Mono.empty());
        StepVerifier.create(service.forgotPassword("missing@example.com"))
                .expectError(com.medic.Web.exception.type.NotFoundException.class)
                .verify();

        UsuarioModel inactive = TestDataFactory.usuarioModel();
        inactive.setAtivo(false);
        when(repository.findByEmail(inactive.getEmail())).thenReturn(Mono.just(inactive));
        StepVerifier.create(service.forgotPassword(inactive.getEmail()))
                .expectError(org.springframework.security.authentication.DisabledException.class)
                .verify();
    }

    @Test
    void shouldRejectAnInvalidResetCode() {

        PasswordResetCodeModel code = TestDataFactory.passwordResetCodeModel();
        when(passwordResetCodeRepository.findFirstByEmailAndUsadoFalseAndExpiraEmAfterOrderByExpiraEmDesc(anyString(), any()))
                .thenReturn(Mono.just(code));
        when(passwordResetCodeGenerator.valide("wrong", code.getCodigo())).thenReturn(false);

        StepVerifier.create(service.resetPassword(new ResetPasswordRequestDTO(
                        "user@example.com", "wrong", "nova")))
                .expectError(com.medic.Web.exception.type.auth.PasswordResetCodeException.class)
                .verify();
    }
}
