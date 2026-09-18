package com.medic.ETL.service.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.service.schedule.job.AtualizarAnvisaJob;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class AnvisaAtualizacaoWorkerTest {

    @Test
    void shouldReleaseTheWorkerWhenThereIsNoClaim() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        AtualizarAnvisaJob job = mock(AtualizarAnvisaJob.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(claimService.claim()).thenReturn(Optional.empty());

        new AnvisaAtualizacaoWorker(claimService, job, executor).dispatch();

        verifyNoInteractions(job, executor);
    }

    @Test
    void shouldReleaseTheWorkerWhenClaimingFails() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(claimService.claim()).thenThrow(new IllegalStateException("falha"));

        assertDoesNotThrow(() -> new AnvisaAtualizacaoWorker(
                claimService, mock(AtualizarAnvisaJob.class), executor
        ).dispatch());

        verifyNoInteractions(executor);
    }

    @Test
    void shouldDispatchTheClaimAndReleaseTheWorkerAfterTheJobFinishes() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        AtualizarAnvisaJob job = mock(AtualizarAnvisaJob.class);
        ExecutorService executor = mock(ExecutorService.class);
        AnvisaAtualizacaoClaim claim = claim();
        when(claimService.claim()).thenReturn(Optional.of(claim));

        var worker = new AnvisaAtualizacaoWorker(claimService, job, executor);
        worker.dispatch();

        var runnable = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(runnable.capture());
        assertDoesNotThrow(runnable.getValue()::run);
        verify(job).run(claim);

        worker.dispatch();
        verify(claimService, times(2)).claim();
    }

    @Test
    void shouldReleaseTheWorkerWhenTheJobFails() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        AtualizarAnvisaJob job = mock(AtualizarAnvisaJob.class);
        ExecutorService executor = mock(ExecutorService.class);
        AnvisaAtualizacaoClaim claim = claim();
        when(claimService.claim()).thenReturn(Optional.of(claim));
        doThrow(new IllegalStateException("falha")).when(job).run(claim);

        var worker = new AnvisaAtualizacaoWorker(claimService, job, executor);
        worker.dispatch();

        var runnable = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(runnable.capture());
        assertThrows(IllegalStateException.class, runnable.getValue()::run);

        when(claimService.claim()).thenReturn(Optional.empty());
        assertDoesNotThrow(worker::dispatch);
    }

    @Test
    void shouldReleaseTheWorkerWhenExecutorRejectsTheTask() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        ExecutorService executor = mock(ExecutorService.class);
        AnvisaAtualizacaoClaim claim = claim();
        when(claimService.claim()).thenReturn(Optional.of(claim));
        doThrow(new IllegalStateException("executor indisponivel"))
                .when(executor).submit(any(Runnable.class));

        var worker = new AnvisaAtualizacaoWorker(
                claimService, mock(AtualizarAnvisaJob.class), executor
        );

        assertDoesNotThrow(worker::dispatch);
        when(claimService.claim()).thenReturn(Optional.empty());
        assertDoesNotThrow(worker::dispatch);
    }

    @Test
    void shouldIgnoreDispatchWhileAnotherTaskIsPending() {

        AnvisaAtualizacaoClaimService claimService = mock(AnvisaAtualizacaoClaimService.class);
        ExecutorService executor = mock(ExecutorService.class);
        AnvisaAtualizacaoClaim claim = claim();
        when(claimService.claim()).thenReturn(Optional.of(claim));

        var worker = new AnvisaAtualizacaoWorker(
                claimService, mock(AtualizarAnvisaJob.class), executor
        );

        worker.dispatch();
        worker.dispatch();

        verify(claimService).claim();
    }

    private AnvisaAtualizacaoClaim claim() {

        Processamento processamento = new Processamento();
        processamento.setId(UUID.randomUUID());
        return new AnvisaAtualizacaoClaim(UUID.randomUUID(), UUID.randomUUID(), processamento);
    }
}
