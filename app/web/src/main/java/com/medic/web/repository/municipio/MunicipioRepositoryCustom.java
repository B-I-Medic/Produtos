package com.medic.web.repository.municipio;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import reactor.core.publisher.Flux;

public interface MunicipioRepositoryCustom {

    Flux<MunicipioResponseDTO> findByFiltro(MunicipioFilterDTO filter);
}
