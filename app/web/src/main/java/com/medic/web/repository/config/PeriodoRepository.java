package com.medic.web.repository.config;

import com.medic.web.model.config.periodo.PeriodoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface PeriodoRepository extends ReactiveCrudRepository<PeriodoModel, UUID> {
}
