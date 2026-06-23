package com.medic.Produtos.repository.cd;

import com.medic.Produtos.model.cd.CentroDistribuicaoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CentroDistribuicaoRepository extends ReactiveCrudRepository<CentroDistribuicaoModel, UUID> {
}
