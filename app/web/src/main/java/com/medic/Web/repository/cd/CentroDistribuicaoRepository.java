package com.medic.Web.repository.cd;

import com.medic.Web.model.cd.CentroDistribuicaoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface CentroDistribuicaoRepository extends ReactiveCrudRepository<CentroDistribuicaoModel, UUID> {
}
