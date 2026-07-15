package com.medic.ETL.config.schedule;

import com.medic.ETL.service.schedule.ConsultaConfigScheduleService;
import com.medic.ETL.service.schedule.job.Job;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    private static final Duration RECHECK_INTERVAL = Duration.ofMinutes(1);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final ConsultaConfigScheduleService consultaConfigScheduleService;
    private final List<Job> jobs;

    public DynamicSchedulingConfig(ConsultaConfigScheduleService consultaConfigScheduleService,
                                   List<Job> jobs) {
        this.consultaConfigScheduleService = consultaConfigScheduleService;
        this.jobs = jobs;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar taskRegistrar) {

        jobs.forEach(
                job -> taskRegistrar.addTriggerTask(
                        () -> executarSeAtivo(job),
                        triggerContext -> consultaConfigScheduleService
                                .getCron(job.getJob())
                                .map(cron -> new CronTrigger(cron, ZONE_ID)
                                                    .nextExecution(triggerContext)
                                )
                                .orElseGet(() ->
                                        triggerContext.getClock()
                                                .instant()
                                                .plus(RECHECK_INTERVAL)
                                )
                )
        );
    }

    private void executarSeAtivo(Job job) {
        consultaConfigScheduleService.getCron(job.getJob())
                .ifPresent(cron -> job.run());
    }
}
