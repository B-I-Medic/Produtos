package com.medic.Web.service.anvisa;

import com.medic.Web.cliente.AnvisaEtlClient;
import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoRequestDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import com.medic.Web.exception.type.AnvisaEtlUnavailableException;
import com.medic.Web.exception.type.NotFoundException;
import com.medic.Web.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.Web.repository.anvisa.AnvisaConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnvisaAtualizacaoServiceTest {

    @Mock
    private AnvisaEtlClient etlClient;
    @Mock
    private AnvisaAtualizacaoRepository atualizacaoRepository;
    @Mock
    private AnvisaConfiguracaoRepository configuracaoRepository;

    @Test
    void shouldForwardSuccessfulConflictAndCooldownStatuses() {

        UUID userId = UUID.randomUUID();
        AnvisaAtualizacaoResponseDTO body = response("SOLICITADA", null);
        AnvisaAtualizacaoService service = service();

        for (HttpStatus status : new HttpStatus[]{HttpStatus.ACCEPTED, HttpStatus.CONFLICT, HttpStatus.TOO_MANY_REQUESTS}) {
            when(etlClient.solicitar(userId)).thenReturn(Mono.just(
                    new AnvisaEtlClient.AnvisaEtlClientResponse(status, "5", body)
            ));

            StepVerifier.create(service.solicitar(userId))
                    .assertNext(result -> {
                        assertEquals(status, result.getStatusCode());
                        assertEquals("5", result.getHeaders().getFirst("Retry-After"));
                        assertEquals(body, result.getBody());
                    })
                    .verifyComplete();
        }
    }

    @Test
    void shouldForwardSuccessWithoutRetryAfter() {

        UUID userId = UUID.randomUUID();
        AnvisaAtualizacaoResponseDTO body = response("CONCLUIDA", null);
        when(etlClient.solicitar(userId)).thenReturn(Mono.just(
                new AnvisaEtlClient.AnvisaEtlClientResponse(HttpStatus.OK, null, body)
        ));

        StepVerifier.create(service().solicitar(userId))
                .assertNext(result -> {
                    assertEquals(HttpStatus.OK, result.getStatusCode());
                    assertEquals(body, result.getBody());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapUnexpectedEtlStatusAndTransportErrors() {

        UUID userId = UUID.randomUUID();
        when(etlClient.solicitar(userId)).thenReturn(Mono.just(
                new AnvisaEtlClient.AnvisaEtlClientResponse(HttpStatus.BAD_GATEWAY, null, null)
        ));
        StepVerifier.create(service().solicitar(userId))
                .expectError(AnvisaEtlUnavailableException.class)
                .verify();

        when(etlClient.solicitar(userId)).thenReturn(Mono.error(new IllegalStateException("indisponivel")));
        StepVerifier.create(service().solicitar(userId))
                .expectError(AnvisaEtlUnavailableException.class)
                .verify();
    }

    @Test
    void shouldStreamACompletedUpdateAndStopAtTheTerminalStatus() {

        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoResponseDTO completed = response("CONCLUIDA", id);
        when(atualizacaoRepository.findById(id)).thenReturn(Mono.just(completed));

        StepVerifier.create(service().acompanhar(id))
                .assertNext(event -> {
                    assertEquals("anvisa-atualizacao", event.event());
                    assertEquals(id.toString(), event.id());
                    assertEquals(completed, event.data());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundWhenThereIsNoUpdateToStream() {

        UUID id = UUID.randomUUID();
        when(atualizacaoRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service().acompanhar(id))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void shouldDelegateReadAndConfigurationOperations() {

        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoResponseDTO today = response("FALHOU", id);
        AnvisaConfiguracaoResponseDTO config = new AnvisaConfiguracaoResponseDTO(5);
        when(atualizacaoRepository.findToday(any(LocalDate.class))).thenReturn(Mono.just(today));
        when(configuracaoRepository.find()).thenReturn(Mono.just(config));
        when(configuracaoRepository.update(10, id)).thenReturn(Mono.just(new AnvisaConfiguracaoResponseDTO(10)));

        StepVerifier.create(service().hoje()).expectNext(today).verifyComplete();
        StepVerifier.create(service().configuracao()).expectNext(config).verifyComplete();
        StepVerifier.create(service().atualizarConfiguracao(new AnvisaConfiguracaoRequestDTO(10), id))
                .expectNext(new AnvisaConfiguracaoResponseDTO(10))
                .verifyComplete();

        when(configuracaoRepository.update(10, id)).thenReturn(Mono.empty());
        StepVerifier.create(service().atualizarConfiguracao(new AnvisaConfiguracaoRequestDTO(10), id))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private AnvisaAtualizacaoService service() {

        return new AnvisaAtualizacaoService(etlClient, atualizacaoRepository, configuracaoRepository);
    }

    private AnvisaAtualizacaoResponseDTO response(String status, UUID id) {

        return new AnvisaAtualizacaoResponseDTO(
                id == null ? UUID.randomUUID() : id,
                LocalDate.of(2026, 9, 18),
                status,
                1,
                Instant.now(),
                null,
                "CONCLUIDA".equals(status) ? Instant.now() : null,
                "FALHOU".equals(status) ? Instant.now() : null,
                null,
                null,
                null
        );
    }
}
