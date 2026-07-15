package com.medic.Web.mapper.config.schedule;

import com.medic.Web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.Web.model.config.schedule.ScheduleModel;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    public ScheduleResponseDTO toDTO(ScheduleModel entity) {

        return new ScheduleResponseDTO(
                entity.getId(),
                entity.getJob(),
                entity.getCron(),
                entity.isAtivo()
        );
    }
}
