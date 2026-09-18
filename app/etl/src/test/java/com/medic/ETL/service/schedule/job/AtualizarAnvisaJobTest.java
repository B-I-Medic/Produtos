package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.service.anvisa.AtualizarAnvisaService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AtualizarAnvisaJobTest {

    private final ControlarProcessamentoService processamentoService = mock(ControlarProcessamentoService.class);
    private final AtualizarAnvisaService atualizarAnvisaService = mock(AtualizarAnvisaService.class);
    private final AnvisaAtualizacaoRepository atualizacaoRepository = mock(AnvisaAtualizacaoRepository.class);
    private final AtualizarAnvisaJob job = new AtualizarAnvisaJob(
            processamentoService,
            atualizarAnvisaService,
            atualizacaoRepository
    );

    @Test
    void shouldCompleteClaimedUpdate() throws Exception {

        Processamento processamento = TestDataFactory.processamento();
        AnvisaAtualizacaoClaim claim = new AnvisaAtualizacaoClaim(
                UUID.randomUUID(), UUID.randomUUID(), processamento
        );

        job.run(claim);

        verify(atualizarAnvisaService).atualizar(processamento);
        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);
        verify(atualizacaoRepository).complete(any(), any(), any());
    }

    @Test
    void shouldMarkClaimedUpdateAsFailed() throws Exception {

        Processamento processamento = TestDataFactory.processamento();
        AnvisaAtualizacaoClaim claim = new AnvisaAtualizacaoClaim(
                UUID.randomUUID(), UUID.randomUUID(), processamento
        );
        doThrow(new IllegalStateException("falha"))
                .when(atualizarAnvisaService)
                .atualizar(processamento);

        job.run(claim);

        verify(processamentoService).encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
        verify(atualizacaoRepository).fail(any(), any(), any(), any());
    }

    @Test
    void shouldUseTheExceptionTypeWhenFailureHasNoMessage() throws Exception {

        Processamento processamento = TestDataFactory.processamento();
        AnvisaAtualizacaoClaim claim = new AnvisaAtualizacaoClaim(
                UUID.randomUUID(), UUID.randomUUID(), processamento
        );
        doThrow(new IllegalStateException())
                .when(atualizarAnvisaService)
                .atualizar(processamento);

        job.run(claim);

        verify(atualizacaoRepository).fail(any(), any(), any(), any());
    }
}
