package com.medic.ETL.service.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.service.schedule.job.AtualizarDemandaJob;
import com.medic.ETL.service.schedule.job.AtualizarEstoqueJob;
import com.medic.ETL.service.schedule.job.AtualizarProdutosJob;
import com.medic.ETL.service.schedule.job.SincronizarEstoqueSegregadoJob;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessamentoInicialListenerTest {

    private final AtualizarProdutosJob produtos = mock(AtualizarProdutosJob.class);
    private final AtualizarEstoqueJob estoque = mock(AtualizarEstoqueJob.class);
    private final AtualizarDemandaJob demanda = mock(AtualizarDemandaJob.class);
    private final SincronizarEstoqueSegregadoJob catalogo = mock(SincronizarEstoqueSegregadoJob.class);
    private final ProcessamentoInicialListener listener =
            new ProcessamentoInicialListener(produtos, estoque, demanda, catalogo);

    @Test
    void shouldRunInitialJobsInDependencyOrder() {
        listener.executarProcessamentosIniciais(mock(ApplicationReadyEvent.class));

        var order = inOrder(catalogo, estoque, demanda, produtos);
        order.verify(catalogo).run();
        order.verify(estoque).run();
        order.verify(demanda).run();
        order.verify(produtos).run();
    }

    @Test
    void shouldContinueRunningJobsWhenOneFails() {
        doThrow(new IllegalStateException("falha no catalogo")).when(catalogo).run();
        when(catalogo.getJob()).thenReturn(ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO);

        assertDoesNotThrow(() -> listener.executarProcessamentosIniciais(mock(ApplicationReadyEvent.class)));

        verify(catalogo).getJob();
        verify(estoque).run();
        verify(demanda).run();
        verify(produtos).run();
    }
}
