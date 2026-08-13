package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.estoque.AtualizarViewMaterializadaRepository;
import com.medic.ETL.repository.processamento.ProcessamentoRepository;
import com.medic.ETL.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.ETL.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.ETL.service.estoque.valePermanente.ProcessarValePermanenteService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarEstoqueJobTest {

    private final AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository = mock(AtualizarViewMaterializadaRepository.class);
    private final ProcessarEstoqueInternoService processarEstoqueInternoService = mock(ProcessarEstoqueInternoService.class);
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService = mock(ProcessarEstoqueSegregadoService.class);
    private final ProcessarValePermanenteService processarValePermanenteService = mock(ProcessarValePermanenteService.class);
    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final ProcessamentoRepository processamentoRepository = mock(ProcessamentoRepository.class);
    private final AtualizarEstoqueJob job = new AtualizarEstoqueJob(
            atualizarViewMaterializadaRepository,
            processarEstoqueInternoService,
            processarEstoqueSegregadoService,
            processarValePermanenteService,
            processamentoService,
            processamentoRepository
    );

    @Test
    void shouldExposeScheduleJob() {
        assertEquals(ScheduleJob.ATUALIZAR_ESTOQUE, job.getJob());
    }

    @Test
    void shouldAbortWhenLockIsAlreadyInUse() {
        when(processamentoRepository.lockEmUso(872341L)).thenReturn(true);

        job.run();

        verify(processamentoService).abortarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);
        verify(processarEstoqueInternoService, never()).processarEstoqueInterno(org.mockito.ArgumentMatchers.any());
        verify(processamentoRepository, never()).liberarLock(872341L);
    }

    @Test
    void shouldProcessStocksRefreshViewAndReleaseLock() {
        Processamento processamento = TestDataFactory.processamento();
        when(processamentoRepository.lockEmUso(872341L)).thenReturn(false);
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);

        job.run();

        var inOrder = inOrder(
                processamentoService,
                processarEstoqueInternoService,
                processarEstoqueSegregadoService,
                processarValePermanenteService,
                atualizarViewMaterializadaRepository,
                processamentoRepository
        );
        inOrder.verify(processamentoService).iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);
        inOrder.verify(processarEstoqueInternoService).processarEstoqueInterno(processamento);
        inOrder.verify(processarEstoqueSegregadoService).processarEstoqueSegregado(processamento);
        inOrder.verify(processarValePermanenteService).processarValePermanente(processamento);
        inOrder.verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);
        inOrder.verify(atualizarViewMaterializadaRepository).atualizar();
        inOrder.verify(processamentoRepository).liberarLock(872341L);
    }

    @Test
    void shouldMarkAsFailedAndReleaseLockWhenProcessingFails() {
        Processamento processamento = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha");
        when(processamentoRepository.lockEmUso(872341L)).thenReturn(false);
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);
        doThrow(failure).when(processarEstoqueSegregadoService).processarEstoqueSegregado(processamento);

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);

        assertEquals(failure, thrown);
        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
        verify(processamentoRepository).liberarLock(872341L);
        verify(atualizarViewMaterializadaRepository, never()).atualizar();
    }
}
