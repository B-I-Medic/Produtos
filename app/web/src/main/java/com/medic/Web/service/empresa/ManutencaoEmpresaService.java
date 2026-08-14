package com.medic.Web.service.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.exception.type.NotFoundException;
import com.medic.Web.mapper.empresa.EmpresaMapper;
import com.medic.Web.model.empresa.EmpresaModel;
import com.medic.Web.repository.cd.MunicipioRepository;
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

    @Transactional(readOnly = true)
    public Flux<EmpresaResponseDTO> listEmpresas() {

        return repository.findAll()
                .flatMap(empresa -> Mono.justOrEmpty(empresa.getMunicipioId())
                        .flatMap(municipioRepository::findById)
                        .map(municipio -> mapper.toDTO(empresa, municipio))
                        .switchIfEmpty(Mono.fromSupplier(() -> mapper.toDTO(empresa))));
    }

    @Transactional(readOnly = true)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresaMunicipioByIDEmpresa(UUID idEmpresa) {

        return repository.listEmpresaMunicipioByIdEmpresa(idEmpresa);
    }

    private <T> Mono<T> municipioNotFound(UUID municipioId) {

        return Mono.error(new NotFoundException("Municipio", municipioId.toString(), "id"));
    }

}
