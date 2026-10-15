package com.medic.web.repository.cd;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.model.municipio.MunicipioModel;
import reactor.core.publisher.Flux;

public interface MunicipioRepositoryCustom {

    Flux<MunicipioModel> findByFiltro(MunicipioFilterDTO filter);
}
