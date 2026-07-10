package com.medic.Web.exception.handler;

import com.medic.Web.dto.web.ErrorResponseDTO;
import com.medic.Web.exception.type.NotFounException;
import com.medic.Web.exception.type.auth.PasswordAlreadySetException;
import com.medic.Web.exception.type.auth.PasswordResetCodeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerExceptionTest {

    private final HandlerException handler = new HandlerException(new ErrorResponseWriter());

    @Test
    void shouldHandleAuthenticationEmailInvalid() {
        var exchange = exchange("/auth/login");

        StepVerifier.create(handler.handleAuthentication(new org.springframework.security.authentication.BadCredentialsException("Email invalido"), exchange))
                .assertNext(body -> assertBody(body, "Autenticacao", "Email invalido", "/auth/login"))
                .verifyComplete();
    }

    @Test
    void shouldHandleAuthenticationPasswordInvalid() {
        var exchange = exchange("/auth/login");

        StepVerifier.create(handler.handleAuthentication(new org.springframework.security.authentication.BadCredentialsException("Senha invalida"), exchange))
                .assertNext(body -> assertBody(body, "Autenticacao", "Senha invalida", "/auth/login"))
                .verifyComplete();
    }

    @Test
    void shouldHandleAuthenticationTokenExpired() {
        var exchange = exchange("/auth/me");

        StepVerifier.create(handler.handleAuthentication(new org.springframework.security.authentication.BadCredentialsException("Token expirado"), exchange))
                .assertNext(body -> assertBody(body, "Autenticacao", "Token expirado", "/auth/me"))
                .verifyComplete();
    }

    @Test
    void shouldHandleAuthenticationUserInactive() {
        var exchange = exchange("/auth/login");

        StepVerifier.create(handler.handleAuthentication(new org.springframework.security.authentication.DisabledException("Usuario inativo"), exchange))
                .assertNext(body -> assertBody(body, "Autenticacao", "Usuario inativo", "/auth/login"))
                .verifyComplete();
    }

    @Test
    void shouldHandleAccessDenied() {
        var exchange = exchange("/usuario/save");

        StepVerifier.create(handler.handleAccessDenied(exchange))
                .assertNext(body -> assertBody(body, "Acesso negado", "Voce nao tem permissao para acessar este recurso", "/usuario/save"))
                .verifyComplete();
    }

    @Test
    void shouldHandleNotFound() {
        var exchange = exchange("/auth/forgot-password");

        StepVerifier.create(handler.handleNotFound(new NotFounException("Usuario", "mail@exemplo.com", "email"), exchange))
                .assertNext(body -> assertBody(body, "Nao encontrado", "O(A) Usuario (email = mail@exemplo.com) não foi encontrado.", "/auth/forgot-password"))
                .verifyComplete();
    }

    @Test
    void shouldHandlePasswordAlreadySet() {
        var exchange = exchange("/auth/first-acess");

        StepVerifier.create(handler.handleBadRequest(new PasswordAlreadySetException(), exchange))
                .assertNext(body -> assertBody(body, "Requisicao invalida", "Senha ja definida", "/auth/first-acess"))
                .verifyComplete();
    }

    @Test
    void shouldHandlePasswordResetCode() {
        var exchange = exchange("/auth/reset-password");

        StepVerifier.create(handler.handleBadRequest(new PasswordResetCodeException("Codigo invalido"), exchange))
                .assertNext(body -> assertBody(body, "Requisicao invalida", "Codigo invalido", "/auth/reset-password"))
                .verifyComplete();
    }

    @Test
    void shouldHandleMalformedBody() {
        var exchange = exchange("/auth/reset-password");

        StepVerifier.create(handler.handleMalformedBody(exchange))
                .assertNext(body -> assertBody(body, "Corpo da requisicao mal formatado", "Corpo da requisicao mal formatado", "/auth/reset-password"))
                .verifyComplete();
    }

    @Test
    void shouldHandleDecodingExceptionAsMalformedBody() {
        var exchange = exchange("/auth/reset-password");

        StepVerifier.create(handler.handleMalformedBody(exchange))
                .assertNext(body -> assertBody(body, "Corpo da requisicao mal formatado", "Corpo da requisicao mal formatado", "/auth/reset-password"))
                .verifyComplete();
    }

    @Test
    void shouldHandleValidationErrorsFromBinding() throws Exception {
        var exchange = exchange("/empresa/save");
        WebExchangeBindException ex = webExchangeBindException();

        StepVerifier.create(handler.handleValidation(ex, exchange))
                .assertNext(body -> assertBody(body, "Corpo da requisicao invalido", "email: O email e obrigatorio", "/empresa/save"))
                .verifyComplete();
    }

    @Test
    void shouldHandleValidationErrorsFromConstraintViolations() {
        var exchange = exchange("/empresa/save");
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("body.email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("deve ser valido");

        StepVerifier.create(handler.handleValidation(new ConstraintViolationException(Set.of(violation)), exchange))
                .assertNext(body -> assertBody(body, "Corpo da requisicao invalido", "body.email: deve ser valido", "/empresa/save"))
                .verifyComplete();
    }

    @Test
    void shouldHandleGenericError() {
        var exchange = exchange("/erro");

        StepVerifier.create(handler.handleGeneric(new IllegalStateException("boom"), exchange))
                .assertNext(body -> assertBody(body, "Erro interno", "Falha inesperada", "/erro"))
                .verifyComplete();
    }

    private static void assertBody(ErrorResponseDTO body, String erro, String descricao, String path) {
        assertEquals(erro, body.erro());
        assertEquals(descricao, body.descricao());
        assertEquals(path, body.path());
    }

    private static MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private static WebExchangeBindException webExchangeBindException() throws Exception {
        Method method = Dummy.class.getDeclaredMethod("handler", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "payload");
        bindingResult.addError(new FieldError("payload", "email", "O email e obrigatorio"));
        return new WebExchangeBindException(parameter, bindingResult);
    }

    private static final class Dummy {
        @SuppressWarnings("unused")
        void handler(String value) {
        }
    }
}
