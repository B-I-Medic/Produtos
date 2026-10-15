package com.medic.web.mapper.config.schedule;

import com.medic.web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.web.model.config.schedule.ScheduleModel;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    public ScheduleResponseDTO toDTO(ScheduleModel entity) {

        return new ScheduleResponseDTO(
                entity.getId(),
                entity.getJob(),
                entity.getCron(),
                entity.isAtivo(),
                entity.getUltimaExecucao()
        );
    }
}
