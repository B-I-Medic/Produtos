package com.medic.Web.repository.parametro;

import com.medic.Web.model.parametro.periodo.PeriodoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface PeriodoRepository extends ReactiveCrudRepository<PeriodoModel, UUID> {
}
