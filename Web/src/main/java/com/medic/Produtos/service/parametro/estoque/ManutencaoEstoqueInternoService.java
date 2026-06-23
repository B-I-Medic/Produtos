package com.medic.Produtos.service.parametro.estoque;

import com.medic.Produtos.dto.parametro.estoque.EstoqueInternoRequestDTO;
import com.medic.Produtos.dto.parametro.estoque.EstoqueInternoResponseDTO;
import com.medic.Produtos.mapper.parametro.estoque.EstoqueInternoMapper;
import com.medic.Produtos.model.parametro.estoque.EstoqueInternoParametroModel;
import com.medic.Produtos.repository.estoque.EstoqueInternoRepository;
import com.medic.Produtos.validator.estoque.ValidadorEmpresaEstoqueService;
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
    public Mono<EstoqueInternoResponseDTO> save(EstoqueInternoRequestDTO dto,
                                                UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueInterno(dto.idEmpresa(), dto.comporSubCd())
                .then(Mono.just(new EstoqueInternoParametroModel()))
                .map(estoqueInterno -> mapper.toEntity(estoqueInterno, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<EstoqueInternoResponseDTO> update(UUID estoqueInternoId,
                                                  EstoqueInternoRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueInterno(dto.idEmpresa(), dto.comporSubCd())
                .then(repository.findById(estoqueInternoId))
                .map(estoqueInterno -> mapper.toEntity(estoqueInterno, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID estoqueInternoId) {

        return repository.deleteById(estoqueInternoId);
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueInternoResponseDTO> listEstoqueInterno() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
