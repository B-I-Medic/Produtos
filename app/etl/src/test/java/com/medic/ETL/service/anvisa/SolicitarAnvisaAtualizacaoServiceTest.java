package com.medic.ETL.service.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.dto.anvisa.AnvisaSolicitacaoResultado;
import com.medic.ETL.model.anvisa.AnvisaAtualizacaoStatus;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.repository.anvisa.AnvisaConfiguracaoRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SolicitarAnvisaAtualizacaoServiceTest {

    private final AnvisaAtualizacaoRepository atualizacaoRepository = mock(AnvisaAtualizacaoRepository.class);
    private final AnvisaConfiguracaoRepository configuracaoRepository = mock(AnvisaConfiguracaoRepository.class);
    private final SolicitarAnvisaAtualizacaoService service = new SolicitarAnvisaAtualizacaoService(
            atualizacaoRepository,
            configuracaoRepository
    );

    @Test
    void shouldAcceptTheFirstRequestOfTheDay() {

        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoRegistro registro = registro(id, AnvisaAtualizacaoStatus.SOLICITADA, null);
        when(atualizacaoRepository.findByDateForUpdate(any(LocalDate.class))).thenReturn(java.util.Optional.of(registro));

        AnvisaSolicitacaoResultado result = service.solicitar(UUID.randomUUID());

        assertEquals(AnvisaSolicitacaoResultado.Tipo.ACEITA, result.tipo());
        assertEquals(id, result.response().id());

        var inOrder = inOrder(atualizacaoRepository);
        inOrder.verify(atualizacaoRepository).insertIfAbsent(any(), any(), any(), any());
        inOrder.verify(atualizacaoRepository).findByDateForUpdate(any(LocalDate.class));
    }

    @Test
    void shouldBlockACompletedUpdateForTheRestOfTheDay() {

        AnvisaAtualizacaoRegistro registro = registro(
                UUID.randomUUID(), AnvisaAtualizacaoStatus.CONCLUIDA, null
        );
        when(atualizacaoRepository.findByDateForUpdate(any(LocalDate.class))).thenReturn(java.util.Optional.of(registro));

        AnvisaSolicitacaoResultado result = service.solicitar(UUID.randomUUID());

        assertEquals(AnvisaSolicitacaoResultado.Tipo.CONCLUIDA, result.tipo());
    }

    @Test
    void shouldReturnTheExistingRunningUpdate() {

        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoRegistro registro = registro(id, AnvisaAtualizacaoStatus.EM_EXECUCAO, null);
        when(atualizacaoRepository.findByDateForUpdate(any(LocalDate.class))).thenReturn(java.util.Optional.of(registro));

        AnvisaSolicitacaoResultado result = service.solicitar(UUID.randomUUID());

        assertEquals(AnvisaSolicitacaoResultado.Tipo.ACEITA, result.tipo());
        assertEquals(id, result.response().id());
    }

    @Test
    void shouldKeepTheCooldownAfterAFailure() {

        Instant failedAt = Instant.now();
        AnvisaAtualizacaoRegistro registro = registro(
                UUID.randomUUID(), AnvisaAtualizacaoStatus.FALHOU, failedAt
        );
        when(atualizacaoRepository.findByDateForUpdate(any(LocalDate.class))).thenReturn(java.util.Optional.of(registro));
        when(configuracaoRepository.getRetryCooldownMinutos()).thenReturn(5);

        AnvisaSolicitacaoResultado result = service.solicitar(UUID.randomUUID());

        assertEquals(AnvisaSolicitacaoResultado.Tipo.COOLDOWN, result.tipo());
        assertEquals(failedAt.plusSeconds(300), result.response().proximaSolicitacaoEm());
    }

    @Test
    void shouldReopenAfterTheCooldown() {

        Instant failedAt = Instant.now().minusSeconds(600);
        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoRegistro failed = registro(id, AnvisaAtualizacaoStatus.FALHOU, failedAt);
        AnvisaAtualizacaoRegistro reopened = registro(id, AnvisaAtualizacaoStatus.SOLICITADA, failedAt);
        when(atualizacaoRepository.findByDateForUpdate(any(LocalDate.class)))
                .thenReturn(java.util.Optional.of(failed), java.util.Optional.of(reopened));
        when(configuracaoRepository.getRetryCooldownMinutos()).thenReturn(5);

        AnvisaSolicitacaoResultado result = service.solicitar(UUID.randomUUID());

        assertEquals(AnvisaSolicitacaoResultado.Tipo.ACEITA, result.tipo());
        assertEquals(failed.ultimoErro(), result.response().ultimoErro());
        assertEquals(failed.ultimaFalhaEm(), result.response().ultimaFalhaEm());
        verify(atualizacaoRepository).reabrir(any(), any(), any());
    }

    private AnvisaAtualizacaoRegistro registro(UUID id,
                                               AnvisaAtualizacaoStatus status,
                                               Instant ultimaFalha) {
        return new AnvisaAtualizacaoRegistro(
                id,
                LocalDate.now(),
                status,
                1,
                UUID.randomUUID(),
                Instant.now(),
                null,
                status == AnvisaAtualizacaoStatus.CONCLUIDA ? Instant.now() : null,
                ultimaFalha,
                "falha",
                null,
                null,
                null
        );
    }
}
