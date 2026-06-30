package com.medic.Web.repository.estoque;

import com.medic.Web.model.parametro.estoque.EstoqueSegregadoParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EstoqueSegregadoRepository extends ReactiveCrudRepository<EstoqueSegregadoParametroModel, UUID> {
}
