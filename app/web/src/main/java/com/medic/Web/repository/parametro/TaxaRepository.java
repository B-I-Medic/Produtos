package com.medic.Web.repository.parametro;

import com.medic.Web.model.parametro.taxa.TaxaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TaxaRepository extends ReactiveCrudRepository<TaxaModel, UUID> {
}
