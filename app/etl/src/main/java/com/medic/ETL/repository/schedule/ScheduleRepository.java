package com.medic.ETL.repository.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.model.schedule.ScheduleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<ScheduleModel, UUID> {

    Optional<ScheduleModel> findByJobAndAtivoTrue(ScheduleJob job);
}
