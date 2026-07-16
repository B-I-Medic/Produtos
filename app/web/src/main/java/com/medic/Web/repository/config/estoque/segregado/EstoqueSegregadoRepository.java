package com.medic.Web.repository.config.estoque.segregado;

import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EstoqueSegregadoRepository extends
        ReactiveCrudRepository<EstoqueSegregadoParametroModel, UUID>,
        EstoqueSegregadoRepositoryCustom
{
}
