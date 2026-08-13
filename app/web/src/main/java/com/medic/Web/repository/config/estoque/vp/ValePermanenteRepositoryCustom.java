package com.medic.Web.repository.config.estoque.vp;

import com.medic.Web.dto.config.estoque.vp.ValePermanenteFilterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteResponseDTO;
import reactor.core.publisher.Flux;

public interface ValePermanenteRepositoryCustom {

    Flux<ValePermanenteResponseDTO> getAllAndFilter(ValePermanenteFilterDTO filter);
}
