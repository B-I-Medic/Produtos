package com.medic.Web.repository.config.estoque.vp;

import com.medic.Web.dto.config.estoque.vp.ValePermanenteFIlterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteResponseDTO;
import reactor.core.publisher.Flux;

public interface ValePermanenteRepositoryCustom {

    Flux<ValePermanenteResponseDTO> getAllAndFilter(ValePermanenteFIlterDTO filter);
}
