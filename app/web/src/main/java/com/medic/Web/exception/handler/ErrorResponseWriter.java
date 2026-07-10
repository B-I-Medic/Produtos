package com.medic.Web.exception.handler;

import com.medic.Web.dto.web.ErrorResponseDTO;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class ErrorResponseWriter {

    public Mono<ErrorResponseDTO> body(HttpStatus status, String erro, String descricao, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(status);
        return Mono.just(new ErrorResponseDTO(
                LocalDateTime.now(),
                erro,
                descricao,
                exchange.getRequest().getPath().value()
        ));
    }

    public Mono<Void> write(HttpStatus status, String erro, String descricao, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = toJson(new ErrorResponseDTO(
                LocalDateTime.now(),
                erro,
                descricao,
                exchange.getRequest().getPath().value()
        )).getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String toJson(ErrorResponseDTO dto) {
        return '{' +
                "\"dataHora\":\"" + escape(dto.dataHora().toString()) + "\"," +
                "\"erro\":\"" + escape(dto.erro()) + "\"," +
                "\"descricao\":\"" + escape(dto.descricao()) + "\"," +
                "\"path\":\"" + escape(dto.path()) + "\"" +
                '}';
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
