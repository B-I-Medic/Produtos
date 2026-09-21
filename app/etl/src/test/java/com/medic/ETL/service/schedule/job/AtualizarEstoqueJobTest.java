package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.estoque.AtualizarViewMaterializadaRepository;
import com.medic.ETL.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.ETL.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.ETL.service.estoque.valePermanente.ProcessarValePermanenteService;
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

class AtualizarEstoqueJobTest {

    private final AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository = mock(AtualizarViewMaterializadaRepository.class);
    private final ProcessarEstoqueInternoService processarEstoqueInternoService = mock(ProcessarEstoqueInternoService.class);
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService = mock(ProcessarEstoqueSegregadoService.class);
    private final ProcessarValePermanenteService processarValePermanenteService = mock(ProcessarValePermanenteService.class);
    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService = mock(RegistrarExecucaoScheduleService.class);
    private final AtualizarEstoqueJob job = new AtualizarEstoqueJob(
            atualizarViewMaterializadaRepository,
            processarEstoqueInternoService,
            processarEstoqueSegregadoService,
            processarValePermanenteService,
            processamentoService,
            registrarExecucaoScheduleService
    );

    @Test
    void shouldExposeScheduleJob() {
        assertEquals(ScheduleJob.ATUALIZAR_ESTOQUE, job.getJob());
    }

    @Test
    void shouldAbortWhenAnotherExecutionIsInProgress() throws Exception {
        Processamento processamento = TestDataFactory.processamento();
        CountDownLatch processingStarted = new CountDownLatch(1);
        CountDownLatch releaseProcessing = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);
        doAnswer(invocation -> {
            processingStarted.countDown();
            if (!releaseProcessing.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tempo limite aguardando a liberacao do processamento");
            }
            return null;
        }).when(processarEstoqueInternoService).processarEstoqueInterno(processamento);

        try {
            Future<?> firstExecution = executor.submit(job::run);

            assertTrue(
                    processingStarted.await(5, TimeUnit.SECONDS),
                    "A primeira execucao nao iniciou a carga"
            );

            job.run();

            verify(processamentoService).abortarProcessamento(
                    ProcessamentoEntidade.ESTOQUE,
                    ProcessamentoDisparo.AUTOMATICO
            );
            verify(processamentoService, times(1)).iniciarProcessamento(
                    ProcessamentoEntidade.ESTOQUE,
                    ProcessamentoDisparo.AUTOMATICO
            );
            verify(registrarExecucaoScheduleService, times(1)).registrarInicio(ScheduleJob.ATUALIZAR_ESTOQUE);
            verify(processarEstoqueInternoService, times(1)).processarEstoqueInterno(processamento);

            releaseProcessing.countDown();
            firstExecution.get(5, TimeUnit.SECONDS);
        } finally {
            releaseProcessing.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldProcessStocksRefreshViewAndReleaseLock() {
        Processamento firstProcessing = TestDataFactory.processamento();
        Processamento secondProcessing = TestDataFactory.processamento();
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(firstProcessing, secondProcessing);

        job.run();
        job.run();

        verify(processamentoService, times(2)).iniciarProcessamento(
                ProcessamentoEntidade.ESTOQUE,
                ProcessamentoDisparo.AUTOMATICO
        );
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_ESTOQUE);
        verify(processarEstoqueInternoService).processarEstoqueInterno(firstProcessing);
        verify(processarEstoqueInternoService).processarEstoqueInterno(secondProcessing);
        verify(processarEstoqueSegregadoService).processarEstoqueSegregado(firstProcessing);
        verify(processarEstoqueSegregadoService).processarEstoqueSegregado(secondProcessing);
        verify(processarValePermanenteService).processarValePermanente(firstProcessing);
        verify(processarValePermanenteService).processarValePermanente(secondProcessing);
        verify(processamentoService).encerrarProcessamento(firstProcessing, ProcessamentoStatus.CONCLUIDO);
        verify(processamentoService).encerrarProcessamento(secondProcessing, ProcessamentoStatus.CONCLUIDO);
        verify(atualizarViewMaterializadaRepository, times(2)).atualizar();
    }

    @Test
    void shouldMarkAsFailedAndReleaseLockWhenProcessingFails() {
        Processamento firstProcessing = TestDataFactory.processamento();
        Processamento secondProcessing = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha");
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(firstProcessing, secondProcessing);
        doThrow(failure)
                .doNothing()
                .when(processarEstoqueSegregadoService)
                .processarEstoqueSegregado(any());

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);
        job.run();

        assertEquals(failure, thrown);
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_ESTOQUE);
        verify(processamentoService).encerrarProcessamento(firstProcessing, ProcessamentoStatus.FALHOU);
        verify(processamentoService).encerrarProcessamento(secondProcessing, ProcessamentoStatus.CONCLUIDO);
        verify(atualizarViewMaterializadaRepository).atualizar();
    }

    @Test
    void shouldReleaseLockWhenStartingProcessingFails() {
        Processamento processamento = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha ao iniciar");
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenThrow(failure)
                .thenReturn(processamento);

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);
        job.run();

        assertEquals(failure, thrown);
        verify(registrarExecucaoScheduleService, times(2)).registrarInicio(ScheduleJob.ATUALIZAR_ESTOQUE);
        verify(processamentoService).encerrarProcessamento(null, ProcessamentoStatus.FALHOU);
        verify(processarEstoqueInternoService).processarEstoqueInterno(processamento);
        verify(processarEstoqueSegregadoService).processarEstoqueSegregado(processamento);
        verify(processarValePermanenteService).processarValePermanente(processamento);
        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);
        verify(atualizarViewMaterializadaRepository).atualizar();
    }
}
