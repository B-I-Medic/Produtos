package com.medic.web.repository.empresa;

import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EmpresaRepositoryCustom {

    Mono<Boolean> existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(UUID idMunicipio);

    Flux<EmpresaResponseDTO> findPageByFilter(EmpresaPaginaDTO filtro, long limite, long deslocamento);

    Mono<Long> countByFilter(EmpresaPaginaDTO filtro);
}
