package com.medic.Web.exception.handler;

import com.medic.Web.dto.web.ErrorResponseDTO;
import com.medic.Web.exception.type.NotFounException;
import com.medic.Web.exception.type.auth.PasswordAlreadySetException;
import com.medic.Web.exception.type.auth.PasswordResetCodeException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.core.codec.DecodingException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class HandlerException {

    private final ErrorResponseWriter writer;

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, DisabledException.class})
    public Mono<ErrorResponseDTO> handleAuthentication(Exception ex, ServerWebExchange exchange) {
        return writer.body(HttpStatus.UNAUTHORIZED, "Autenticacao", resolveAuthenticationMessage(ex), exchange);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ErrorResponseDTO> handleAccessDenied(ServerWebExchange exchange) {
        return writer.body(HttpStatus.FORBIDDEN, "Acesso negado", "Voce nao tem permissao para acessar este recurso", exchange);
    }

    @ExceptionHandler(NotFounException.class)
    public Mono<ErrorResponseDTO> handleNotFound(NotFounException ex, ServerWebExchange exchange) {
        return writer.body(HttpStatus.NOT_FOUND, "Nao encontrado", ex.getMessage(), exchange);
    }

    @ExceptionHandler({
            PasswordAlreadySetException.class,
            PasswordResetCodeException.class
    })
    public Mono<ErrorResponseDTO> handleBadRequest(RuntimeException ex, ServerWebExchange exchange) {
        return writer.body(HttpStatus.BAD_REQUEST, "Requisicao invalida", ex.getMessage(), exchange);
    }

    @ExceptionHandler({ServerWebInputException.class, DecodingException.class})
    public Mono<ErrorResponseDTO> handleMalformedBody(ServerWebExchange exchange) {
        return writer.body(HttpStatus.BAD_REQUEST, "Corpo da requisicao mal formatado", "Corpo da requisicao mal formatado", exchange);
    }

    @ExceptionHandler({WebExchangeBindException.class, ConstraintViolationException.class})
    public Mono<ErrorResponseDTO> handleValidation(Exception ex, ServerWebExchange exchange) {
        return writer.body(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido", resolveValidationMessage(ex), exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponseDTO> handleGeneric(Exception ex, ServerWebExchange exchange) {
        log.error("Erro nao tratado", ex);
        return writer.body(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Falha inesperada", exchange);
    }

    private String resolveAuthenticationMessage(Exception ex) {
        if (ex instanceof DisabledException) {
            return "Usuario inativo";
        }

        String message = Objects.toString(ex.getMessage(), "Credenciais invalidas");
        return switch (message) {
            case "Email invalido" -> "Email invalido";
            case "Senha invalida" -> "Senha invalida";
            case "Token expirado" -> "Token expirado";
            case "Token invalido" -> "Token invalido";
            case "Credenciais invalidas" -> "Credenciais invalidas";
            default -> message;
        };
    }

    private String resolveValidationMessage(Exception ex) {
        if (ex instanceof WebExchangeBindException bindException) {
            return bindException.getAllErrors().stream()
                    .map(error -> {
                        if (error instanceof FieldError fieldError) {
                            return fieldError.getField() + ": " + Objects.toString(fieldError.getDefaultMessage(), "invalido");
                        }
                        return Objects.toString(error.getDefaultMessage(), "invalido");
                    })
                    .collect(Collectors.joining("; "));
        }

        if (ex instanceof ConstraintViolationException violationException) {
            return violationException.getConstraintViolations().stream()
                    .map(violation -> {
                        String path = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
                        return path.isBlank()
                                ? violation.getMessage()
                                : path + ": " + violation.getMessage();
                    })
                    .collect(Collectors.joining("; "));
        }

        return Objects.toString(ex.getMessage(), "Corpo da requisicao invalido");
    }
}
