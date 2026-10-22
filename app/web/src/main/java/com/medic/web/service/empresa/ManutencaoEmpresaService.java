package com.medic.web.service.empresa;

import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.exception.type.NotFoundException;
import com.medic.web.mapper.empresa.EmpresaMapper;
import com.medic.web.model.empresa.EmpresaModel;
import com.medic.web.repository.municipio.MunicipioRepository;
import com.medic.web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEmpresaService {

    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;
    private final MunicipioRepository municipioRepository;

    public ManutencaoEmpresaService(EmpresaRepository repository,
                                    EmpresaMapper mapper,
                                    MunicipioRepository municipioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.municipioRepository = municipioRepository;
    }

    @Transactional
    public Mono<EmpresaResponseDTO> save(EmpresaRequestDTO dto, UUID userId) {

        return municipioRepository.findById(dto.municipioId())
                .switchIfEmpty(municipioNotFound(dto.municipioId()))
                .flatMap(municipio -> Mono.just(new EmpresaModel())
                        .map(empresa -> mapper.toEntity(empresa, dto, userId))
                        .flatMap(repository::save)
                        .map(empresa -> mapper.toDTO(empresa, municipio)));
    }

    @Transactional
    public Mono<EmpresaResponseDTO> update(UUID empresaId,
                                           EmpresaRequestDTO dto,
                                           UUID userId) {

        return municipioRepository.findById(dto.municipioId())
                .switchIfEmpty(municipioNotFound(dto.municipioId()))
                .flatMap(municipio -> repository.findById(empresaId)
                        .map(empresa -> mapper.toEntity(empresa, dto, userId))
                        .flatMap(repository::save)
                        .map(empresa -> mapper.toDTO(empresa, municipio)));
    }

    @Transactional
    public Mono<Void> delete(UUID empresaId) {

        return repository.deleteById(empresaId);
    }

    private <T> Mono<T> municipioNotFound(UUID municipioId) {

        return Mono.error(new NotFoundException("Municipio", municipioId.toString(), "id"));
    }

}
