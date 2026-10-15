package com.medic.web.repository.empresa;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EmpresaRepositoryCustom {

    Mono<Boolean> existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(UUID idMunicipio);
}
