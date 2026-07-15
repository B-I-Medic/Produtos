package com.medic.Web.repository.config;

import com.medic.Web.model.config.schedule.ScheduleModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ScheduleRepository extends ReactiveCrudRepository<ScheduleModel, UUID> {
}
