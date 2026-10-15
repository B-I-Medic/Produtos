package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.model.config.estoque.EstoqueSegregadoCatalogoModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface EstoqueSegregadoRepository extends
        ReactiveCrudRepository<EstoqueSegregadoCatalogoModel, UUID>,
        EstoqueSegregadoRepositoryCustom
{
}
