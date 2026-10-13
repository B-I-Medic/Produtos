package com.medic.Web.repository.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EmpresaRepositoryCustom {

    Flux<EmpresaMunicipioResponseDTO> listEmpresaMunicipioByIdEmpresa(UUID idEmpresa);

    Mono<Boolean> findByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(UUID idMunicipio);
}
