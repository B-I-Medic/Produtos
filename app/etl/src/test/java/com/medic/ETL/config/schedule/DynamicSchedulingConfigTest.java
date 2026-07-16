package com.medic.ETL.config.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.service.schedule.ConsultaConfigScheduleService;
import com.medic.ETL.service.schedule.job.Job;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.TriggerContext;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicSchedulingConfigTest {

    @Mock
    private ConsultaConfigScheduleService consultaConfigScheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private Job jobAtivo;

    @Mock
    private Job jobInativo;

    @Mock
    private ScheduledTaskRegistrar taskRegistrar;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<Trigger> triggerCaptor;

    @Test
    void shouldRegisterTasksAndUseCronOrRecheckFallback() {

        when(jobAtivo.getJob()).thenReturn(ScheduleJob.ATUALIZAR_ESTOQUE);
        when(jobInativo.getJob()).thenReturn(ScheduleJob.ATUALIZAR_DEMANDA);
        when(consultaConfigScheduleService.getCron(ScheduleJob.ATUALIZAR_ESTOQUE))
                .thenReturn(Optional.of("0 0 * * * *"));
        when(consultaConfigScheduleService.getCron(ScheduleJob.ATUALIZAR_DEMANDA))
                .thenReturn(Optional.empty());

        new DynamicSchedulingConfig(consultaConfigScheduleService, scheduleRepository, List.of(jobAtivo, jobInativo))
                .configureTasks(taskRegistrar);

        verify(taskRegistrar, times(2)).addTriggerTask(runnableCaptor.capture(), triggerCaptor.capture());

        var fixedInstant = Instant.parse("2026-07-15T15:30:00Z");
        var triggerContext = triggerContext(fixedInstant);

        var cronExecution = triggerCaptor.getAllValues().get(0).nextExecution(triggerContext);
        var fallbackExecution = triggerCaptor.getAllValues().get(1).nextExecution(triggerContext);

        assertNotNull(cronExecution);
        assertTrue(cronExecution.isAfter(fixedInstant.plus(Duration.ofMinutes(1))));
        assertEquals(fixedInstant.plus(Duration.ofMinutes(1)), fallbackExecution);

        runnableCaptor.getAllValues().get(0).run();
        runnableCaptor.getAllValues().get(1).run();

        verify(scheduleRepository).atualizarUltimaExecucao(ScheduleJob.ATUALIZAR_ESTOQUE, fixedInstant);
        verify(scheduleRepository).atualizarUltimaExecucao(ScheduleJob.ATUALIZAR_DEMANDA, fixedInstant);
        verify(jobAtivo).run();
        verify(jobInativo, never()).run();
    }

    private static TriggerContext triggerContext(Instant instant) {

        var clock = Clock.fixed(instant, ZoneOffset.UTC);
        return new TriggerContext() {
            @Override
            public Instant lastScheduledExecution() {
                return null;
            }

            @Override
            public Instant lastActualExecution() {
                return null;
            }

            @Override
            public Instant lastCompletion() {
                return null;
            }

            @Override
            public Clock getClock() {
                return clock;
            }
        };
    }
}
