package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.processamento.ProcessamentoCustomRepositoryImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LimparProcessamentosRetroativosJobTest {

    private final ProcessamentoCustomRepositoryImpl processamentoRepository = mock(ProcessamentoCustomRepositoryImpl.class);
    private final LimparProcessamentosRetroativosJob job = new LimparProcessamentosRetroativosJob(processamentoRepository);

    @Test
    void shouldExposeScheduleJob() {
        assertEquals(ScheduleJob.EXCLUIR_PROCESSAMENTOS_ANTIGOS, job.getJob());
    }

    @Test
    void shouldDeleteOldProcessingRows() {
        job.run();

        verify(processamentoRepository).excluirProcessamentosAntigos();
    }
}
