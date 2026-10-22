package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EstoqueSegregadoRepositoryCustom {

    Flux<EstoqueSegregadoResponseDTO> findPageByEmpresaAndFilter(UUID idEmpresa,
                                                                  EstoqueSegregadoPaginaDTO filtro,
                                                                  long limite,
                                                                  long deslocamento);

    Mono<Long> countByEmpresaAndFilter(UUID idEmpresa, EstoqueSegregadoPaginaDTO filtro);
}
