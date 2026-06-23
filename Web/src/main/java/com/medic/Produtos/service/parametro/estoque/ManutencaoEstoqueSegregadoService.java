package com.medic.Produtos.service.parametro.estoque;

import com.medic.Produtos.dto.parametro.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Produtos.dto.parametro.estoque.EstoqueSegregadoResponseDTO;
import com.medic.Produtos.mapper.parametro.estoque.EstoqueSegregadoMapper;
import com.medic.Produtos.model.parametro.estoque.EstoqueSegregadoParametroModel;
import com.medic.Produtos.repository.estoque.EstoqueSegregadoRepository;
import com.medic.Produtos.validator.estoque.ValidadorEmpresaEstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEstoqueSegregadoService {

    private final EstoqueSegregadoRepository repository;
    private final EstoqueSegregadoMapper mapper;
    private final ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService;

    public ManutencaoEstoqueSegregadoService(EstoqueSegregadoRepository repository,
                                             EstoqueSegregadoMapper mapper,
                                             ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService) {
        this.repository = repository;
        this.mapper = mapper;
        this.validadorEmpresaEstoqueService = validadorEmpresaEstoqueService;
    }

    @Transactional
    public Mono<EstoqueSegregadoResponseDTO> save(EstoqueSegregadoRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueSegregado(dto.idEmpresa(), dto.comporSubCd())
                .then(Mono.just(new EstoqueSegregadoParametroModel()))
                .map(estoqueSegregado -> mapper.toEntity(estoqueSegregado, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<EstoqueSegregadoResponseDTO> update(UUID estoqueSegregadoId,
                                                    EstoqueSegregadoRequestDTO dto,
                                                    UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueSegregado(dto.idEmpresa(), dto.comporSubCd())
                .then(repository.findById(estoqueSegregadoId))
                .map(estoqueSegregado -> mapper.toEntity(estoqueSegregado, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID estoqueSegregadoId) {

        return repository.deleteById(estoqueSegregadoId);
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
