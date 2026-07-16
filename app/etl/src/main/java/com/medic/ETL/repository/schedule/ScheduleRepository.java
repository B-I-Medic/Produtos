package com.medic.ETL.repository.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.model.schedule.ScheduleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<ScheduleModel, UUID> {

    Optional<ScheduleModel> findByJobAndAtivoTrue(ScheduleJob job);

    @Modifying
    @Transactional
    @Query("update ScheduleModel schedule set schedule.ultimaExecucao = :ultimaExecucao where schedule.job = :job")
    void atualizarUltimaExecucao(@Param("job") ScheduleJob job,
                                @Param("ultimaExecucao") Instant ultimaExecucao);
}
