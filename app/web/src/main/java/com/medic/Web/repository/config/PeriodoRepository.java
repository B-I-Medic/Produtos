package com.medic.Web.repository.config;

import com.medic.Web.model.config.periodo.PeriodoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface PeriodoRepository extends ReactiveCrudRepository<PeriodoModel, UUID> {
}
