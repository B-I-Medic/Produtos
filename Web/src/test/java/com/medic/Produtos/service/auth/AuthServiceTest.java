package com.medic.Produtos.service.auth;

import com.medic.Produtos.dto.auth.LoginRequestDTO;
import com.medic.Produtos.dto.auth.PasswordRequestDTO;
import com.medic.Produtos.dto.auth.ResetPasswordRequestDTO;
import com.medic.Produtos.mapper.auth.PasswordResetCodeMapper;
import com.medic.Produtos.model.auth.PasswordResetCodeModel;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.repository.auth.PasswordResetCodeRepository;
import com.medic.Produtos.repository.usuario.UsuarioRepository;
import com.medic.Produtos.service.mail.MailService;
import com.medic.Produtos.service.mail.MailTemplateService;
import com.medic.Produtos.support.TestDataFactory;
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
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.getExpires_in("token")).thenReturn(Instant.now().plusSeconds(3600));

        StepVerifier.create(service.login(new LoginRequestDTO(user.getEmail(), "123")))
                .expectNextMatches(response -> response.email().equals(user.getEmail()) && response.token().equals("token"))
                .verifyComplete();
    }

    @Test
    void shouldFirstAccess() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("nova")).thenReturn("hash");
        when(repository.save(user)).thenReturn(Mono.just(user));

        StepVerifier.create(service.firstAcess(new PasswordRequestDTO("nova"), user.getId()))
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

        StepVerifier.create(service.resetPassword(new ResetPasswordRequestDTO(user.getEmail(), "123456", "nova")))
                .verifyComplete();
    }
}
