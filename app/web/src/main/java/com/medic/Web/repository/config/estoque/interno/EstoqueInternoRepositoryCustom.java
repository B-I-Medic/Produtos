package com.medic.Web.repository.config.estoque.interno;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoFIlterDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoResponseDTO;
import reactor.core.publisher.Flux;

public interface EstoqueInternoRepositoryCustom {

    Flux<EstoqueInternoResponseDTO> getAllAndFilter(EstoqueInternoFIlterDTO filter);
}
