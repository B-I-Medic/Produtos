package com.medic.Web.controller.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.model.processamento.ProcessamentoDisparo;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.model.processamento.ProcessamentoStatus;
import com.medic.Web.service.processamento.ConsultaProcessamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessamentoControllerTest {

    private final ConsultaProcessamentoService service = mock(ConsultaProcessamentoService.class);
    private WebTestClient client;

    @BeforeEach
    void setUp() {

        client = WebTestClient.bindToController(new ProcessamentoController(service)).build();
    }

    @Test
    void shouldListLatestProcessingByEntity() {

        when(service.consultarUltimosPorEntidade()).thenReturn(Flux.just(processamento()));

        client.get()
                .uri("/processamento/get")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);

        verify(service).consultarUltimosPorEntidade();
    }

    private static ProcessamentoResponseDTO processamento() {

        return new ProcessamentoResponseDTO(
                ProcessamentoEntidade.ESTOQUE,
                UUID.randomUUID(),
                ProcessamentoStatus.CONCLUIDO,
                ProcessamentoDisparo.AUTOMATICO,
                Instant.parse("2026-09-21T17:00:00Z"),
                Instant.parse("2026-09-21T17:05:00Z")
        );
    }
}
