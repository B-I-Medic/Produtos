package com.medic.web.repository.cd;

import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CentroDistribuicaoRepositoryCustom {

    Flux<CentroDistribuicaoResponseDTO> findPageByFilter(CentroDistribuicaoPaginaDTO filtro, long limite, long deslocamento);

    Mono<Long> countByFilter(CentroDistribuicaoPaginaDTO filtro);
}
