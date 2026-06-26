package com.medic.Produtos.repository.parametro;

import com.medic.Produtos.model.parametro.taxa.TaxaModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface TaxaRepository extends ReactiveCrudRepository<TaxaModel, UUID> {
}
