package com.medic.Web.service.config.estoque;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoFilterDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoResponseDTO;
import com.medic.Web.mapper.config.estoque.EstoqueInternoMapper;
import com.medic.Web.model.config.estoque.EstoqueInternoParametroModel;
import com.medic.Web.repository.config.estoque.interno.EstoqueInternoRepository;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEstoqueInternoService {

    private final EstoqueInternoRepository repository;
    private final EstoqueInternoMapper mapper;
    private final ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService;

    public ManutencaoEstoqueInternoService(EstoqueInternoRepository repository,
                                           EstoqueInternoMapper mapper,
                                           ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService) {
        this.repository = repository;
        this.mapper = mapper;
        this.validadorEmpresaEstoqueService = validadorEmpresaEstoqueService;
    }

    @Transactional
    public Mono<Void> save(EstoqueInternoRequestDTO dto,
                                                UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueInterno(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(Mono.just(new EstoqueInternoParametroModel()))
                .map(estoqueInterno -> mapper.toEntity(estoqueInterno, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> update(UUID estoqueInternoId,
                                                  EstoqueInternoRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueInterno(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(repository.findById(estoqueInternoId))
                .map(estoqueInterno -> mapper.toEntity(estoqueInterno, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> delete(UUID estoqueInternoId) {

        return repository.deleteById(estoqueInternoId);
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueInternoResponseDTO> listEstoqueInterno(EstoqueInternoFilterDTO filter) {

        return repository.getAllAndFilter(filter);
    }
}
