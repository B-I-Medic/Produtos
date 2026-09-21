package com.medic.Web.cliente;

import com.medic.Web.config.properties.AnvisaEtlProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnvisaEtlClientTest {

    @Test
    void shouldSendTheInternalApiKeyAndMapTheEtlResponse() {

        AnvisaEtlProperties properties = properties("secret");
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            request.set(invocation.getArgument(0));
            return Mono.just(response(HttpStatus.ACCEPTED, "12"));
        });

        AnvisaEtlClient client = client(properties, exchangeFunction);

        StepVerifier.create(client.solicitar(UUID.randomUUID()))
                .assertNext(result -> {
                    assertEquals(HttpStatus.ACCEPTED, result.status());
                    assertEquals("12", result.retryAfter());
                    assertEquals("SOLICITADA", result.body().status());
                })
                .verifyComplete();

        assertEquals("secret", request.get().headers().getFirst("X-Internal-Api-Key"));
        assertEquals(MediaType.APPLICATION_JSON_VALUE,
                request.get().headers().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("/internal/anvisa/atualizacoes", request.get().url().getPath());
    }

    @Test
    void shouldOmitTheApiKeyWhenItIsBlank() {

        AnvisaEtlProperties properties = properties(" ");
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        AtomicReference<ClientRequest> request = new AtomicReference<>();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenAnswer(invocation -> {
            request.set(invocation.getArgument(0));
            return Mono.just(response(HttpStatus.CONFLICT, null));
        });

        StepVerifier.create(client(properties, exchangeFunction).solicitar(UUID.randomUUID()))
                .assertNext(result -> {
                    assertEquals(HttpStatus.CONFLICT, result.status());
                    assertNull(result.retryAfter());
                    assertEquals(0, result.body().tentativas());
                })
                .verifyComplete();

        assertNull(request.get().headers().getFirst("X-Internal-Api-Key"));
    }

    private AnvisaEtlClient client(AnvisaEtlProperties properties,
                                   ExchangeFunction exchangeFunction) {

        AnvisaEtlClient client = new AnvisaEtlClient(properties);
        ReflectionTestUtils.setField(
                client,
                "webClient",
                WebClient.builder().exchangeFunction(exchangeFunction).build()
        );
        return client;
    }

    private AnvisaEtlProperties properties(String apiKey) {

        AnvisaEtlProperties properties = new AnvisaEtlProperties();
        properties.setBaseUrl("http://etl.test");
        properties.setApiKey(apiKey);
        return properties;
    }

    private ClientResponse response(HttpStatus status, String retryAfter) {

        var builder = ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (retryAfter != null) {
            builder.header(HttpHeaders.RETRY_AFTER, retryAfter);
        }

        return builder.body("{\"status\":\"SOLICITADA\",\"tentativas\":0}").build();
    }
}
