package com.medic.ETL.config.schedule;

import com.medic.ETL.service.schedule.ConsultaConfigScheduleService;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import com.medic.ETL.service.schedule.job.Job;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    private static final Duration RECHECK_INTERVAL = Duration.ofMinutes(1);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final ConsultaConfigScheduleService consultaConfigScheduleService;
    private final ScheduleRepository scheduleRepository;
    private final List<Job> jobs;

    public DynamicSchedulingConfig(ConsultaConfigScheduleService consultaConfigScheduleService,
                                   ScheduleRepository scheduleRepository,
                                   List<Job> jobs) {
        this.consultaConfigScheduleService = consultaConfigScheduleService;
        this.scheduleRepository = scheduleRepository;
        this.jobs = jobs;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {

        jobs.forEach(
                job -> taskRegistrar.addTriggerTask(
                        () -> executarSeAtivo(job),
                        triggerContext -> calcularProximaExecucao(job, triggerContext)
                )
        );
    }

    private void executarSeAtivo(Job job) {
        consultaConfigScheduleService.getCron(job.getJob())
                .ifPresent(cron -> job.run());
    }

    private Instant calcularProximaExecucao(Job job, TriggerContext triggerContext) {

        Instant ultimaExecucao = triggerContext.getClock().instant();
        scheduleRepository.atualizarUltimaExecucao(job.getJob(), ultimaExecucao);

        return consultaConfigScheduleService
                .getCron(job.getJob())
                .map(cron -> new CronTrigger(cron, ZONE_ID)
                        .nextExecution(triggerContext)
                )
                .orElseGet(() -> ultimaExecucao.plus(RECHECK_INTERVAL));
    }
}
