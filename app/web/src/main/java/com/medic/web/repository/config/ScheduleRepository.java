package com.medic.web.repository.config;

import com.medic.web.model.config.schedule.ScheduleModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ScheduleRepository extends ReactiveCrudRepository<ScheduleModel, UUID> {
}
