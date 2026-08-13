package com.medic.Web.dto.config.schedule;

import com.medic.Web.model.config.schedule.ScheduleJob;

import java.time.Instant;
import java.util.UUID;

public record ScheduleResponseDTO(

        UUID id,
        ScheduleJob job,
        String cron,
        Boolean ativo,
        Instant ultima_execucao

) {
}
