package com.medic.web.repository.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MunicipioRepositoryCustom {

    Flux<MunicipioResponseDTO> findPageByFilter(MunicipioPaginaDTO filtro, long limite, long deslocamento);

    Mono<Long> countByFilter(MunicipioPaginaDTO filtro);
}
