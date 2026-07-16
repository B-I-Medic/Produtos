package com.medic.Web.service.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFIlterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.Web.mapper.config.estoque.EstoqueSegregadoMapper;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
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
    public Mono<Void> save(EstoqueSegregadoRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueSegregado(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(Mono.just(new EstoqueSegregadoParametroModel()))
                .map(estoqueSegregado -> mapper.toEntity(estoqueSegregado, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> update(UUID estoqueSegregadoId,
                                                    EstoqueSegregadoRequestDTO dto,
                                                    UUID userId) {

        return validadorEmpresaEstoqueService.validarEstoqueSegregado(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(repository.findById(estoqueSegregadoId))
                .map(estoqueSegregado -> mapper.toEntity(estoqueSegregado, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> delete(UUID estoqueSegregadoId) {

        return repository.deleteById(estoqueSegregadoId);
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado(EstoqueSegregadoFIlterDTO filter) {

        return repository.getAllAndFilter(filter);
    }
}
