package com.medic.Produtos.repository.estoque;

import com.medic.Produtos.model.parametro.estoque.ValePermanenteParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ValePermanenteRepository extends ReactiveCrudRepository<ValePermanenteParametroModel, UUID> {
}
