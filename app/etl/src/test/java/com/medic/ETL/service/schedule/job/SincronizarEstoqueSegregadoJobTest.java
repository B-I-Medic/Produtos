package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.service.estoque.segregado.SincronizarEstoqueSegregadoCatalogoService;
import com.medic.ETL.service.schedule.RegistrarExecucaoScheduleService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SincronizarEstoqueSegregadoJobTest {

    private final SincronizarEstoqueSegregadoCatalogoService service = mock(SincronizarEstoqueSegregadoCatalogoService.class);
    private final RegistrarExecucaoScheduleService registrar = mock(RegistrarExecucaoScheduleService.class);
    private final SincronizarEstoqueSegregadoJob job = new SincronizarEstoqueSegregadoJob(service, registrar);

    @Test
    void shouldExposeScheduleAndRunSynchronization() {
        assertEquals(ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO, job.getJob());

        job.run();

        verify(registrar).registrarInicio(ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO);
        verify(service).sincronizar();
    }

    @Test
    void shouldPropagateFailureAndReleaseLockForNextRun() {
        RuntimeException failure = new IllegalStateException("falha na sincronizacao");
        doThrow(failure).doNothing().when(service).sincronizar();

        assertEquals(failure, assertThrows(RuntimeException.class, job::run));
        job.run();

        verify(registrar, times(2)).registrarInicio(ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO);
        verify(service, times(2)).sincronizar();
    }

    @Test
    void shouldSkipSynchronizationWhileAnotherRunHoldsTheLock() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        doAnswer(invocation -> {
            started.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("tempo limite esperando liberar o job");
            }
            return null;
        }).when(service).sincronizar();

        try {
            Future<?> firstRun = executor.submit(job::run);
            assertTrue(started.await(5, TimeUnit.SECONDS));

            job.run();

            verify(service, times(1)).sincronizar();
            verify(registrar, times(1)).registrarInicio(ScheduleJob.SINCRONIZAR_ESTOQUE_SEGREGADO);
            release.countDown();
            firstRun.get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}
