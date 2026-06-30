package com.medic.Web.repository.estoque;

import com.medic.Web.model.parametro.estoque.ValePermanenteParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ValePermanenteRepository extends ReactiveCrudRepository<ValePermanenteParametroModel, UUID> {
}
