package com.medic.Produtos.repository.estoque;

import com.medic.Produtos.model.parametro.estoque.EstoqueInternoParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EstoqueInternoRepository extends ReactiveCrudRepository<EstoqueInternoParametroModel, UUID> {
}
