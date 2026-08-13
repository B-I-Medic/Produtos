package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.processamento.ProcessamentoCustomRepository;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.service.produto.ProcessarProdutoService;
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

class AtualizarProdutosJobTest {

    private final ProcessamentoCustomRepository processamentoRepository = mock(ProcessamentoCustomRepository.class);
    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final ProcessarProdutoService processarProdutoService = mock(ProcessarProdutoService.class);
    private final AtualizarProdutosJob job = new AtualizarProdutosJob(
            processamentoRepository,
            processamentoService,
            processarProdutoService
    );

    @Test
    void shouldExposeScheduleJob() {
        assertEquals(ScheduleJob.ATUALIZAR_PRODUTOS, job.getJob());
    }

    @Test
    void shouldAbortWhenLockIsAlreadyInUse() {
        when(processamentoRepository.lockEmUso(872342L)).thenReturn(true);

        job.run();

        verify(processamentoService).abortarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO);
        verify(processarProdutoService, never()).atualizarProdutos(org.mockito.ArgumentMatchers.any());
        verify(processamentoRepository, never()).liberarLock(872342L);
    }

    @Test
    void shouldProcessAndReleaseLock() {
        Processamento processamento = TestDataFactory.processamento();
        when(processamentoRepository.lockEmUso(872342L)).thenReturn(false);
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);

        job.run();

        var inOrder = inOrder(processamentoService, processarProdutoService, processamentoRepository);
        inOrder.verify(processamentoService).iniciarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO);
        inOrder.verify(processarProdutoService).atualizarProdutos(processamento);
        inOrder.verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);
        inOrder.verify(processamentoRepository).liberarLock(872342L);
    }

    @Test
    void shouldMarkAsFailedAndReleaseLockWhenProcessingFails() {
        Processamento processamento = TestDataFactory.processamento();
        RuntimeException failure = new RuntimeException("falha");
        when(processamentoRepository.lockEmUso(872342L)).thenReturn(false);
        when(processamentoService.iniciarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO))
                .thenReturn(processamento);
        doThrow(failure).when(processarProdutoService).atualizarProdutos(processamento);

        RuntimeException thrown = assertThrows(RuntimeException.class, job::run);

        assertEquals(failure, thrown);
        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
        verify(processamentoRepository).liberarLock(872342L);
    }
}
