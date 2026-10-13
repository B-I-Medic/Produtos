package com.medic.Web.service.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.Web.mapper.config.estoque.EstoqueSegregadoMapper;
import com.medic.Web.model.config.estoque.EstoqueSegregadoParametroModel;
import com.medic.Web.repository.cd.MunicipioRepository;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEstoqueSegregadoService {

    private final MunicipioRepository municipioRepository;
    private final EstoqueSegregadoRepository repository;
    private final EstoqueSegregadoMapper mapper;
    private final ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService;

    public ManutencaoEstoqueSegregadoService(MunicipioRepository municipioRepository,
                                             EstoqueSegregadoRepository repository,
                                             EstoqueSegregadoMapper mapper,
                                             ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService) {
        this.municipioRepository = municipioRepository;
        this.repository = repository;
        this.mapper = mapper;
        this.validadorEmpresaEstoqueService = validadorEmpresaEstoqueService;
    }

    @Transactional
    public Mono<Void> save(EstoqueSegregadoRequestDTO dto,
                           UUID userId) {

        return validadorEmpresaEstoqueService.validarEmpresa(dto.idEmpresa())
                .then(Mono.just(new EstoqueSegregadoParametroModel())
                .flatMap(estoqueSegregadoParametro -> municipioRepository.findById(dto.idMunicipio())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Municipio não encontrado")))
                        .map(municipio -> mapper.toEntity(estoqueSegregadoParametro, dto, municipio, userId))))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> update(UUID estoqueSegregadoId,
                             EstoqueSegregadoRequestDTO dto,
                             UUID userId) {

        return validadorEmpresaEstoqueService.validarEmpresa(dto.idEmpresa())
                .then(repository.findById(estoqueSegregadoId))
                .flatMap(estoqueSegregado -> municipioRepository.findById(dto.idMunicipio())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Municipio não encontrado")))
                        .map(municipio -> mapper.toEntity(estoqueSegregado, dto, municipio, userId)))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> delete(UUID estoqueSegregadoId) {

        return repository.deleteById(estoqueSegregadoId);
    }

    @Transactional(readOnly = true)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado(EstoqueSegregadoFilterDTO filter) {

        return repository.getAllAndFilter(filter);
    }
}
