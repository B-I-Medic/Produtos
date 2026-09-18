package com.medic.Web.cliente;

import com.medic.Web.config.properties.AnvisaEtlProperties;
import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AnvisaEtlClient {

    private final WebClient webClient;
    private final AnvisaEtlProperties properties;

    public AnvisaEtlClient(AnvisaEtlProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public Mono<AnvisaEtlClientResponse> solicitar(UUID usuarioId) {

        WebClient.RequestBodySpec request = webClient.post()
                .uri("/internal/anvisa/atualizacoes")
                .header(HttpHeaders.CONTENT_TYPE, "application/json");

        if (StringUtils.hasText(properties.getApiKey())) {
            request.header("X-Internal-Api-Key", properties.getApiKey());
        }

        return request.bodyValue(new AnvisaEtlRequest(usuarioId))
                .exchangeToMono(response -> response
                        .toEntity(AnvisaAtualizacaoResponseDTO.class)
                        .map(entity -> new AnvisaEtlClientResponse(
                                response.statusCode(),
                                entity.getHeaders().getFirst(HttpHeaders.RETRY_AFTER),
                                entity.getBody()
                        )));
    }

    public record AnvisaEtlClientResponse(
            HttpStatusCode status,
            String retryAfter,
            AnvisaAtualizacaoResponseDTO body
    ) {
    }

    private record AnvisaEtlRequest(UUID solicitadoPor) {}
}
