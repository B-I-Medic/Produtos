package com.medic.Web.mapper.config.schedule;

import com.medic.Web.model.config.schedule.ScheduleJob;
import com.medic.Web.model.config.schedule.ScheduleModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScheduleMapperTest {

    private final ScheduleMapper mapper = new ScheduleMapper();

    @Test
    void shouldKeepScheduleExecutionAsInstant() {

        Instant execution = Instant.parse("2026-08-10T13:00:00Z");
        ScheduleModel model = scheduleModel(execution);

        assertEquals(execution, mapper.toDTO(model).ultima_execucao());
    }

    @Test
    void shouldAllowScheduleWithoutExecution() {

        assertNull(mapper.toDTO(scheduleModel(null)).ultima_execucao());
    }

    private static ScheduleModel scheduleModel(Instant execution) {

        ScheduleModel model = new ScheduleModel();
        model.setJob(ScheduleJob.ATUALIZAR_ESTOQUE);
        model.setCron("0 0 * * * *");
        model.setAtivo(true);
        model.setUltimaExecucao(execution);
        return model;
    }
}
