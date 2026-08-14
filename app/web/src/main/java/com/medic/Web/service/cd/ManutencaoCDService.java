package com.medic.Web.service.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.Web.model.cd.CentroDistribuicaoModel;
import com.medic.Web.exception.type.NotFoundException;
import com.medic.Web.repository.cd.CentroDistribuicaoRepository;
import com.medic.Web.repository.cd.MunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoCDService {

    private final CentroDistribuicaoRepository repository;
    private final CentroDistribuicaoMapper mapper;
    private final MunicipioRepository municipioRepository;

    public ManutencaoCDService(CentroDistribuicaoRepository repository,
                               CentroDistribuicaoMapper mapper,
                               MunicipioRepository municipioRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.municipioRepository = municipioRepository;
    }

    @Transactional
    public Mono<CentroDistribuicaoResponseDTO> save(CentroDistribuicaoRequestDTO dto,
                                                    UUID userId) {

        return municipioRepository.findById(dto.municipioId())
                .switchIfEmpty(municipioNotFound(dto.municipioId()))
                .flatMap(municipio -> Mono.just(new CentroDistribuicaoModel())
                        .map(cd -> mapper.toEntity(cd, dto, userId))
                        .flatMap(repository::save)
                        .map(cd -> mapper.toDTO(cd, municipio)));
    }

    @Transactional
    public Mono<CentroDistribuicaoResponseDTO> update(UUID cdId,
                                                      CentroDistribuicaoRequestDTO dto,
                                                      UUID userId) {

        return municipioRepository.findById(dto.municipioId())
                .switchIfEmpty(municipioNotFound(dto.municipioId()))
                .flatMap(municipio -> repository.findById(cdId)
                        .map(cd -> mapper.toEntity(cd, dto, userId))
                        .flatMap(repository::save)
                        .map(cd -> mapper.toDTO(cd, municipio)));
    }

    @Transactional
    public Mono<Void> delete(UUID cdId) {

        return repository.deleteById(cdId);
    }

    @Transactional(readOnly = true)
    public Flux<CentroDistribuicaoResponseDTO> listCDs() {

        return repository.findAll()
                .flatMap(cd -> Mono.justOrEmpty(cd.getMunicipioId())
                        .flatMap(municipioRepository::findById)
                        .map(municipio -> mapper.toDTO(cd, municipio))
                        .switchIfEmpty(Mono.fromSupplier(() -> mapper.toDTO(cd))));
    }

    private <T> Mono<T> municipioNotFound(UUID municipioId) {

        return Mono.error(new NotFoundException("Municipio", municipioId.toString(), "id"));
    }
}
