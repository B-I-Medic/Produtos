package com.medic.etl.repository.periodo;

import com.medic.etl.model.periodo.PeriodoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PeriodoRepository extends JpaRepository<PeriodoModel, UUID> {
}
