package com.medic.Web.repository.cd;

import com.medic.Web.dto.cd.CdEmpresaMunicipioFilterDTO;
import com.medic.Web.dto.cd.CdEmpresaMunicipioResponseDTO;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CdEmpresaMunipioRepositoryCustom {

    Flux<CdEmpresaMunicipioResponseDTO> findByFiltro(UUID cdId,
                                                     CdEmpresaMunicipioFilterDTO filter);
}
