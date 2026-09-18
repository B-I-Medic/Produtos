package com.medic.ETL.service.anvisa;

import com.medic.ETL.config.property.AnvisaProperties;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.model.anvisa.AnvisaAtualizacaoStatus;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AnvisaAtualizacaoClaimServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AnvisaAtualizacaoRepository atualizacaoRepository = mock(AnvisaAtualizacaoRepository.class);
    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final EntityManager entityManager = mock(EntityManager.class);

    private AnvisaAtualizacaoClaimService service;

    @BeforeEach
    void setUp() {

        AnvisaProperties properties = new AnvisaProperties();
        properties.getWorker().setLeaseSeconds(1800);

        service = new AnvisaAtualizacaoClaimService(atualizacaoRepository, processamentoService, properties);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @Test
    void shouldStartARequestedUpdateFromTheCurrentDate() {

        LocalDate currentDate = LocalDate.now(ZONE_ID);
        AnvisaAtualizacaoRegistro registro = registro(AnvisaAtualizacaoStatus.SOLICITADA, currentDate, null);
        Processamento processamento = processamento();

        when(atualizacaoRepository.findNextForClaim(any(Instant.class), eq(currentDate)))
                .thenReturn(Optional.of(registro));
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ANVISA, ProcessamentoDisparo.MANUAL))
                .thenReturn(processamento);

        Optional<AnvisaAtualizacaoClaim> result = service.claim();

        assertTrue(result.isPresent());
        assertEquals(registro.id(), result.get().atualizacaoId());
        verify(entityManager).flush();
        verify(atualizacaoRepository).start(
                eq(registro.id()),
                eq(processamento.getId()),
                any(UUID.class),
                any(Instant.class),
                any(Instant.class)
        );
    }

    @Test
    void shouldRecordAnInterruptedAttemptAndRetryOnTheCurrentDate() {

        LocalDate currentDate = LocalDate.now(ZONE_ID);
        UUID processamentoAnteriorId = UUID.randomUUID();
        AnvisaAtualizacaoRegistro registro = registro(
                AnvisaAtualizacaoStatus.EM_EXECUCAO,
                currentDate,
                processamentoAnteriorId
        );
        Processamento novoProcessamento = processamento();

        when(atualizacaoRepository.findNextForClaim(any(Instant.class), eq(currentDate)))
                .thenReturn(Optional.of(registro));
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ANVISA, ProcessamentoDisparo.MANUAL))
                .thenReturn(novoProcessamento);

        Optional<AnvisaAtualizacaoClaim> result = service.claim();

        assertTrue(result.isPresent());
        verify(atualizacaoRepository).markInterruptedAttemptAsFailed(eq(processamentoAnteriorId), any(Instant.class));
        verify(atualizacaoRepository).resetExpiredForRetry(
                eq(registro.id()),
                any(Instant.class),
                eq("A execucao anterior foi interrompida; uma nova tentativa foi iniciada no mesmo dia.")
        );
        verify(atualizacaoRepository).start(
                eq(registro.id()),
                eq(novoProcessamento.getId()),
                any(UUID.class),
                any(Instant.class),
                any(Instant.class)
        );
    }

    @Test
    void shouldFailAStaleRequestWithoutStartingAnAttempt() {

        LocalDate currentDate = LocalDate.now(ZONE_ID);
        AnvisaAtualizacaoRegistro registro = registro(
                AnvisaAtualizacaoStatus.SOLICITADA,
                currentDate.minusDays(1),
                null
        );

        when(atualizacaoRepository.findNextForClaim(any(Instant.class), eq(currentDate)))
                .thenReturn(Optional.of(registro));

        Optional<AnvisaAtualizacaoClaim> result = service.claim();

        assertTrue(result.isEmpty());
        verify(atualizacaoRepository).failStale(
                eq(registro.id()),
                any(Instant.class),
                eq("A solicitacao nao foi iniciada antes do encerramento do dia de referencia.")
        );
        verify(atualizacaoRepository, never()).markInterruptedAttemptAsFailed(any(), any());
        verifyNoInteractions(processamentoService, entityManager);
    }

    @Test
    void shouldFailAStaleRunningUpdateWithoutRetryingIt() {

        LocalDate currentDate = LocalDate.now(ZONE_ID);
        UUID processamentoId = UUID.randomUUID();
        AnvisaAtualizacaoRegistro registro = registro(
                AnvisaAtualizacaoStatus.EM_EXECUCAO,
                currentDate.minusDays(1),
                processamentoId
        );

        when(atualizacaoRepository.findNextForClaim(any(Instant.class), eq(currentDate)))
                .thenReturn(Optional.of(registro));

        Optional<AnvisaAtualizacaoClaim> result = service.claim();

        assertTrue(result.isEmpty());
        verify(atualizacaoRepository).markInterruptedAttemptAsFailed(eq(processamentoId), any(Instant.class));
        verify(atualizacaoRepository).failStale(
                eq(registro.id()),
                any(Instant.class),
                eq("A execucao foi interrompida e nao sera retomada em outro dia.")
        );
        verifyNoInteractions(processamentoService, entityManager);
    }

    @Test
    void shouldReturnEmptyWhenThereIsNoCandidate() {

        LocalDate currentDate = LocalDate.now(ZONE_ID);
        when(atualizacaoRepository.findNextForClaim(any(Instant.class), eq(currentDate)))
                .thenReturn(Optional.empty());

        Optional<AnvisaAtualizacaoClaim> result = service.claim();

        assertTrue(result.isEmpty());
        verifyNoInteractions(processamentoService, entityManager);
    }

    private AnvisaAtualizacaoRegistro registro(AnvisaAtualizacaoStatus status,
                                               LocalDate dataReferencia,
                                               UUID processamentoId) {

        return new AnvisaAtualizacaoRegistro(
                UUID.randomUUID(),
                dataReferencia,
                status,
                status == AnvisaAtualizacaoStatus.SOLICITADA ? 0 : 1,
                UUID.randomUUID(),
                Instant.now(),
                status == AnvisaAtualizacaoStatus.EM_EXECUCAO ? Instant.now() : null,
                null,
                null,
                null,
                processamentoId,
                status == AnvisaAtualizacaoStatus.EM_EXECUCAO ? UUID.randomUUID() : null,
                status == AnvisaAtualizacaoStatus.EM_EXECUCAO ? Instant.now().minusSeconds(1) : null
        );
    }

    private Processamento processamento() {

        Processamento processamento = new Processamento();
        processamento.setId(UUID.randomUUID());
        return processamento;
    }
}
