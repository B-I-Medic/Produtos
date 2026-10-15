package com.medic.web.repository.cd;

import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import reactor.core.publisher.Flux;

public interface CentroDistribuicaoRepositoryCustom {

    Flux<CentroDistribuicaoResponseDTO> findAllWithMunicipio();
}
