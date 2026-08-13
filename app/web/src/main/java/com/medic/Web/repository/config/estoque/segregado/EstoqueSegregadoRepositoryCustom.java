package com.medic.Web.repository.config.estoque.segregado;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import reactor.core.publisher.Flux;

public interface EstoqueSegregadoRepositoryCustom {

    Flux<EstoqueSegregadoResponseDTO> getAllAndFilter(EstoqueSegregadoFilterDTO filter);
}
