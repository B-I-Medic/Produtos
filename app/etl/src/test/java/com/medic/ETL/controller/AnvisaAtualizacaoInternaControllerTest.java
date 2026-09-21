package com.medic.ETL.controller;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoResponse;
import com.medic.ETL.dto.anvisa.AnvisaSolicitacaoResultado;
import com.medic.ETL.dto.anvisa.SolicitarAnvisaAtualizacaoRequest;
import com.medic.ETL.service.anvisa.SolicitarAnvisaAtualizacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnvisaAtualizacaoInternaControllerTest {

    private final SolicitarAnvisaAtualizacaoService service = mock(SolicitarAnvisaAtualizacaoService.class);
    private final AnvisaAtualizacaoInternaController controller = new AnvisaAtualizacaoInternaController(service);
    private final UUID userId = UUID.randomUUID();

    @Test
    void shouldReturnTooManyRequestsWithRetryAfterDuringCooldown() {

        AnvisaAtualizacaoResponse response = response(Instant.now().plusSeconds(120));
        when(service.solicitar(userId)).thenReturn(
                new AnvisaSolicitacaoResultado(response, AnvisaSolicitacaoResultado.Tipo.COOLDOWN)
        );

        ResponseEntity<AnvisaAtualizacaoResponse> result = controller.solicitar(
                new SolicitarAnvisaAtualizacaoRequest(userId)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, result.getStatusCode());
        assertNotNull(result.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
        assertEquals(response, result.getBody());
        verify(service).solicitar(userId);
    }

    @Test
    void shouldReturnConflictWhenTheUpdateWasAlreadyCompleted() {

        AnvisaAtualizacaoResponse response = response(null);
        when(service.solicitar(userId)).thenReturn(
                new AnvisaSolicitacaoResultado(response, AnvisaSolicitacaoResultado.Tipo.CONCLUIDA)
        );

        ResponseEntity<AnvisaAtualizacaoResponse> result = controller.solicitar(
                new SolicitarAnvisaAtualizacaoRequest(userId)
        );

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void shouldAcceptANewRequest() {

        AnvisaAtualizacaoResponse response = response(null);
        when(service.solicitar(any(UUID.class))).thenReturn(
                new AnvisaSolicitacaoResultado(response, AnvisaSolicitacaoResultado.Tipo.ACEITA)
        );

        ResponseEntity<AnvisaAtualizacaoResponse> result = controller.solicitar(
                new SolicitarAnvisaAtualizacaoRequest(userId)
        );

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    private AnvisaAtualizacaoResponse response(Instant proximaSolicitacaoEm) {

        return new AnvisaAtualizacaoResponse(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 18),
                "FALHOU",
                1,
                Instant.now(),
                null,
                null,
                Instant.now(),
                proximaSolicitacaoEm,
                null,
                "falha"
        );
    }
}
