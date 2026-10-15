package com.medic.web.repository.cd;

import com.medic.web.model.cd.CentroDistribuicaoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CentroDistribuicaoRepository extends ReactiveCrudRepository<CentroDistribuicaoModel, UUID> {
}
