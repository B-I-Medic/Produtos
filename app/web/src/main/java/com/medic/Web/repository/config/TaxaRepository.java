package com.medic.Web.repository.config;

import com.medic.Web.model.config.taxa.TaxaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TaxaRepository extends ReactiveCrudRepository<TaxaModel, UUID> {
}
