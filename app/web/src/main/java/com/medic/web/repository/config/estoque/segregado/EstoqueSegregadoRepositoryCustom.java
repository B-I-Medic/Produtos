package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import reactor.core.publisher.Flux;

public interface EstoqueSegregadoRepositoryCustom {

    Flux<EstoqueSegregadoResponseDTO> getAllAndFilter(EstoqueSegregadoFilterDTO filter);
}
