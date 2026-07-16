package com.medic.Web.service.config.estoque;

import com.medic.Web.dto.config.estoque.vp.ValePermanenteFIlterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteRequestDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteResponseDTO;
import com.medic.Web.mapper.config.estoque.ValePermanenteMapper;
import com.medic.Web.model.config.estoque.ValePermanenteParametroModel;
import com.medic.Web.repository.config.estoque.vp.ValePermanenteRepository;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoValePermanenteService {

    private final ValePermanenteRepository repository;
    private final ValePermanenteMapper mapper;
    private final ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService;

    public ManutencaoValePermanenteService(ValePermanenteRepository repository,
                                           ValePermanenteMapper mapper,
                                           ValidadorEmpresaEstoqueService validadorEmpresaEstoqueService) {
        this.repository = repository;
        this.mapper = mapper;
        this.validadorEmpresaEstoqueService = validadorEmpresaEstoqueService;
    }

    @Transactional
    public Mono<Void> save(ValePermanenteRequestDTO dto,
                                                UUID userId) {

        return validadorEmpresaEstoqueService.validarValePermanente(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(Mono.just(new ValePermanenteParametroModel()))
                .map(valePermanente -> mapper.toEntity(valePermanente, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> update(UUID valePermanenteId,
                                                  ValePermanenteRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarValePermanente(dto.idEmpresa(), dto.id_empresa_municipio())
                .then(repository.findById(valePermanenteId))
                .map(valePermanente -> mapper.toEntity(valePermanente, dto, userId))
                .flatMap(repository::save)
                .then();
    }

    @Transactional
    public Mono<Void> delete(UUID valePermanenteId) {

        return repository.deleteById(valePermanenteId);
    }

    @Transactional(readOnly = true)
    public Flux<ValePermanenteResponseDTO> listValePermanente(ValePermanenteFIlterDTO filter) {

        return repository.getAllAndFilter(filter);
    }
}
