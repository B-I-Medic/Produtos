package com.medic.Produtos.repository.necessidade;

import com.medic.Produtos.model.necessidade.NecessidadeModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface NecessidadeRepository extends ReactiveCrudRepository<NecessidadeModel, UUID> {
}
