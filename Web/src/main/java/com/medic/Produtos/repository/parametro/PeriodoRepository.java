package com.medic.Produtos.repository.parametro;

import com.medic.Produtos.model.parametro.periodo.PeriodoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface PeriodoRepository extends ReactiveCrudRepository<PeriodoModel, UUID> {
}
