package com.medic.Web.service.cd;

import com.medic.Web.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Web.dto.cd.CdEmpresaMunipioResponseDTO;
import com.medic.Web.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Web.model.cd.CdEmpresaMunipioModel;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Web.repository.empresa.EmpresaMunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoCdEmpresaMunicipioService {

    private final CdEmpresaMunipioRepository repository;
    private final EmpresaMunicipioRepository empresaMunicipioRepository;
    private final CdEmpresaMunipioMapper mapper;

    public ManutencaoCdEmpresaMunicipioService(CdEmpresaMunipioRepository repository,
                                               EmpresaMunicipioRepository empresaMunicipioRepository,
                                               CdEmpresaMunipioMapper mapper) {
        this.repository = repository;
        this.empresaMunicipioRepository = empresaMunicipioRepository;
        this.mapper = mapper;
    }

    @Transactional
    public Mono<CdEmpresaMunipioResponseDTO> save(CdEmpresaMunipioRequestDTO dto,
                                                  UUID userId) {

        return Mono.just(new CdEmpresaMunipioModel())
                .map(cdEmpresaMunipio -> mapper.toEntity(cdEmpresaMunipio, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID cdEmpresaMunicipioId) {

        return repository.findById(cdEmpresaMunicipioId)
                .flatMap(cdEmpresaMunicipio -> repository.delete(cdEmpresaMunicipio)
                        .then(repository.findByIdEmpresaMunicipio(cdEmpresaMunicipio.getIdEmpresaMunicipio())
                                .hasElements()
                                .flatMap(possuiVinculo -> {

                                    if (possuiVinculo)
                                        return Mono.empty();

                                    return empresaMunicipioRepository.deleteById(cdEmpresaMunicipio.getIdEmpresaMunicipio());
                                })))
                .then();
    }

    @Transactional(readOnly = true)
    public Flux<CdEmpresaMunipioResponseDTO> listCdEmpresaMunicipio() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
