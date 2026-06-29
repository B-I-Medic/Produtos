package com.medic.Web.controller.auth;

import com.medic.Web.dto.auth.LoginRequestDTO;
import com.medic.Web.dto.auth.LoginResponseDTO;
import com.medic.Web.dto.auth.PasswordRequestDTO;
import com.medic.Web.dto.auth.ResetPasswordRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.auth.AuthService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final AuthService service = mock(AuthService.class);
    private WebTestClient client;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        client = WebTestClient.bindToController(new AuthController(service))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldLogin() {

        LoginResponseDTO response = new LoginResponseDTO("Teste", "teste@medic.com", TestDataFactory.usuarioModel().getRole(), false, "token", Instant.now());
        when(service.login(new LoginRequestDTO("teste@medic.com", "123"))).thenReturn(Mono.just(response));

        client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequestDTO("teste@medic.com", "123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("teste@medic.com");
    }

    @Test
    void shouldFirstAccess() {

        when(service.firstAcess(new PasswordRequestDTO("nova"), user.getId())).thenReturn(Mono.empty());

        client.post()
                .uri("/auth/first-acess")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PasswordRequestDTO("nova"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldForgotPassword() {

        when(service.forgotPassword("teste@medic.com")).thenReturn(Mono.empty());

        client.post()
                .uri(uriBuilder -> uriBuilder.path("/auth/forgot-password").queryParam("mail", "teste@medic.com").build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldResetPassword() {

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO("teste@medic.com", "123456", "nova");
        when(service.resetPassword(dto)).thenReturn(Mono.empty());

        client.post()
                .uri("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();
    }
}
