package com.medic.ETL.repository.periodo;

import com.medic.ETL.model.periodo.PeriodoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PeriodoRepository extends JpaRepository<PeriodoModel, UUID> {
}
