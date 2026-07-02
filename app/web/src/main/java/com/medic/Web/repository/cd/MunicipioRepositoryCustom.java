package com.medic.Web.repository.cd;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.model.municipio.MunicipioModel;
import reactor.core.publisher.Flux;

public interface MunicipioRepositoryCustom {

    Flux<MunicipioModel> findByFiltro(MunicipioFilterDTO filter);
}
