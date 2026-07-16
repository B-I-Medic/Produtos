package com.medic.Web.repository.config.estoque.vp;

import com.medic.Web.model.config.estoque.ValePermanenteParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ValePermanenteRepository extends
        ReactiveCrudRepository<ValePermanenteParametroModel, UUID>,
        ValePermanenteRepositoryCustom
{
}
