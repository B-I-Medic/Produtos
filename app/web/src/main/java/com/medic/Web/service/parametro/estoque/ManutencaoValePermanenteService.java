package com.medic.Web.service.parametro.estoque;

import com.medic.Web.dto.parametro.estoque.ValePermanenteRequestDTO;
import com.medic.Web.dto.parametro.estoque.ValePermanenteResponseDTO;
import com.medic.Web.mapper.parametro.estoque.ValePermanenteMapper;
import com.medic.Web.model.parametro.estoque.ValePermanenteParametroModel;
import com.medic.Web.repository.estoque.ValePermanenteRepository;
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
    public Mono<ValePermanenteResponseDTO> save(ValePermanenteRequestDTO dto,
                                                UUID userId) {

        return validadorEmpresaEstoqueService.validarValePermanente(dto.idEmpresa(), dto.comporSubCd())
                .then(Mono.just(new ValePermanenteParametroModel()))
                .map(valePermanente -> mapper.toEntity(valePermanente, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<ValePermanenteResponseDTO> update(UUID valePermanenteId,
                                                  ValePermanenteRequestDTO dto,
                                                  UUID userId) {

        return validadorEmpresaEstoqueService.validarValePermanente(dto.idEmpresa(), dto.comporSubCd())
                .then(repository.findById(valePermanenteId))
                .map(valePermanente -> mapper.toEntity(valePermanente, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID valePermanenteId) {

        return repository.deleteById(valePermanenteId);
    }

    @Transactional(readOnly = true)
    public Flux<ValePermanenteResponseDTO> listValePermanente() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
