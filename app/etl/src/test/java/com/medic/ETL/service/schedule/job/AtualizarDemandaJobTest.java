package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.service.demanda.ProcessarDemandaService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.service.schedule.RegistrarExecucaoScheduleService;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarDemandaJobTest {

    private final ProcessarDemandaService processarDemandaService = mock(ProcessarDemandaService.class);
    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService = mock(RegistrarExecucaoScheduleService.class);
    private final AtualizarDemandaJob job = new AtualizarDemandaJob(
            processarDemandaService,
            processamentoService,
            registrarExecucaoScheduleService
    );

    @Test
    void shouldExposeScheduleJob() {
        assertEquals(ScheduleJob.ATUALIZAR_DEMANDA, job.getJob());
    }

    @Test
    void shouldAbortWhenAnotherExecutionIsInProgress() throws Exception {
        Processamento processamento = TestDataFactory.processamento();
        CountDownLatch processingStarted = new CountDownLatch(1);
        CountDownLatch releaseProcessing = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);
        doAnswer(invocation -> {
            processingStarted.countDown();
            if (!releaseProcessing.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tempo limite aguardando a liberacao do processamento");
            }
            return null;
        }).when(processarDemandaService).atualizarDemanda(processamento);

        try {
            Future<?> firstExecution = executor.submit(job::run);

            assertTrue(
                    processingStarted.await(5, TimeUnit.SECONDS),
                    "A primeira execucao nao iniciou a carga"
            );

            job.run();

            verify(processamentoService).abortarProcessamento(
                    ProcessamentoEntidade.DEMANDA,
                    ProcessamentoDisparo.AUTOMATICO
            );
            verify(processamentoService, times(1)).iniciarProcessamento(
                    ProcessamentoEntidade.DEMANDA,
                    ProcessamentoDisparo.AUTOMATICO
            );
            verify(registrarExecucaoScheduleService, times(1)).registrarInicio(ScheduleJob.ATUALIZAR_DEMANDA);
            verify(processarDemandaService, times(1)).atualizarDemanda(processamento);

            releaseProcessing.countDown();
            firstExecution.get(5, TimeUnit.SECONDS);
        } finally {
            releaseProcessing.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldCompleteProcessingAndReleaseLock() {
        Processamento firstProcessing = TestDataFactory.processamento();
        Processamento secondProcessing = TestDataFactory.processamento();
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(firstProcessing, secondProcessing);

        job.run();
        job.run();

        verify(processamentoService, times(2)).iniciarProcessamento(
                ProcessamentoEntidade.DEMANDA,
                ProcessamentoDisparo.AUTOMATICO
        );
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_DEMANDA);
        verify(processarDemandaService).atualizarDemanda(firstProcessing);
        verify(processarDemandaService).atualizarDemanda(secondProcessing);
        verify(processamentoService).encerrarProcessamento(firstProcessing, ProcessamentoStatus.CONCLUIDO);
        verify(processamentoService).encerrarProcessamento(secondProcessing, ProcessamentoStatus.CONCLUIDO);
    }

    @Test
    void shouldMarkAsFailedAndReleaseLockWhenProcessingFails() {
        Processamento firstProcessing = TestDataFactory.processamento();
        Processamento secondProcessing = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha");
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(firstProcessing, secondProcessing);
        doThrow(failure)
                .doNothing()
                .when(processarDemandaService)
                .atualizarDemanda(any());

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);
        job.run();

        assertEquals(failure, thrown);
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_DEMANDA);
        verify(processamentoService).encerrarProcessamento(firstProcessing, ProcessamentoStatus.FALHOU);
        verify(processamentoService).encerrarProcessamento(secondProcessing, ProcessamentoStatus.CONCLUIDO);
    }

    @Test
    void shouldReleaseLockWhenStartingProcessingFails() {
        Processamento processamento = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha ao iniciar");
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO))
                .thenThrow(failure)
                .thenReturn(processamento);

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);
        job.run();

        assertEquals(failure, thrown);
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_DEMANDA);
        verify(processamentoService).encerrarProcessamento(null, ProcessamentoStatus.FALHOU);
        verify(processarDemandaService).atualizarDemanda(processamento);
        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);
    }
}
