package com.medic.web.repository.config;

import com.medic.web.model.config.taxa.TaxaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TaxaRepository extends ReactiveCrudRepository<TaxaModel, UUID> {
}
