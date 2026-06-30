package com.medic.Web.service.empresa;

import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.mapper.empresa.EmpresaMapper;
import com.medic.Web.model.empresa.EmpresaModel;
import com.medic.Web.repository.empresa.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEmpresaService {

    private final EmpresaRepository repository;
    private final EmpresaMapper mapper;

    public ManutencaoEmpresaService(EmpresaRepository repository,
                                    EmpresaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Mono<EmpresaResponseDTO> save(EmpresaRequestDTO dto, UUID userId) {

        return Mono.just(new EmpresaModel())
                .map(empresa -> mapper.toEntity(empresa, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<EmpresaResponseDTO> update(UUID empresaId,
                                           EmpresaRequestDTO dto,
                                           UUID userId) {

        return repository.findById(empresaId)
                .map(empresa -> mapper.toEntity(empresa, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID empresaId) {

        return repository.deleteById(empresaId);
    }

    @Transactional(readOnly = true)
    public Flux<EmpresaResponseDTO> listEmpresas() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
