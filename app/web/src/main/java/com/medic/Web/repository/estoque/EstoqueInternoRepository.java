package com.medic.Web.repository.estoque;

import com.medic.Web.model.config.estoque.EstoqueInternoParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EstoqueInternoRepository extends ReactiveCrudRepository<EstoqueInternoParametroModel, UUID> {
}
