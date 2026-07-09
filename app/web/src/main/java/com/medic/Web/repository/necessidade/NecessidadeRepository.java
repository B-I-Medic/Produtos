package com.medic.Web.repository.necessidade;

import com.medic.Web.model.necessidade.NecessidadeModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface NecessidadeRepository extends
        ReactiveCrudRepository<NecessidadeModel, UUID>,
        NecessidadeRepositoryCustom {
}
