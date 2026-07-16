package com.medic.Web.repository.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EmpresaMunipioRepositoryCustom {

    Flux<EmpresaMunicipioResponseDTO> getAllAndFilter(UUID empresaId,
                                                      EmpresaMunicipioFilterDTO filter);

    Mono<EmpresaMunicipioResponseDTO> findByIdCustom(UUID id);
}
