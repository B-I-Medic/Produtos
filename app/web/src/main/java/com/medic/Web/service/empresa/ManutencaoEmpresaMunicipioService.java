package com.medic.Web.service.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Web.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Web.mapper.empresa.EmpresaMunicipioMapper;
import com.medic.Web.model.cd.CdEmpresaMunicipioModel;
import com.medic.Web.model.empresa.EmpresaMunicipioModel;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Web.repository.empresa.EmpresaMunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoEmpresaMunicipioService {

    private final EmpresaMunicipioRepository repository;
    private final CdEmpresaMunipioRepository cdEmpresaMunipioRepository;

    private final EmpresaMunicipioMapper mapper;
    private final CdEmpresaMunipioMapper cdEmpresaMunipioMapper;

    public ManutencaoEmpresaMunicipioService(EmpresaMunicipioRepository repository,
                                             CdEmpresaMunipioRepository cdEmpresaMunipioRepository,
                                             EmpresaMunicipioMapper mapper,
                                             CdEmpresaMunipioMapper cdEmpresaMunipioMapper) {
        this.repository = repository;
        this.cdEmpresaMunipioRepository = cdEmpresaMunipioRepository;
        this.mapper = mapper;
        this.cdEmpresaMunipioMapper = cdEmpresaMunipioMapper;
    }

    @Transactional
    public Mono<EmpresaMunicipioResponseDTO> save(EmpresaMunicipioRequestDTO dto,
                                                  UUID userId) {

        return Mono.just(new EmpresaMunicipioModel())
                .map(empresaMunicipio -> mapper.toEntity(empresaMunicipio, dto.empresaId(), dto.municipioId(), userId))
                .flatMap(repository::save)
                .flatMap(empresaMunicipio -> cdEmpresaMunipioRepository
                        .save(
                                cdEmpresaMunipioMapper.toEntity(
                                        new CdEmpresaMunicipioModel(),
                                        dto.cdId(),
                                        empresaMunicipio.getId(),
                                        userId
                                )
                        )
                        .thenReturn(empresaMunicipio))
                .flatMap(empresaMunicipio -> findById(empresaMunicipio.getId()));
    }

    @Transactional
    public Mono<Void> delete(UUID empresaMunicipioId) {

        return repository.deleteById(empresaMunicipioId);
    }

    @Transactional(readOnly = true)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresasMunicipio(EmpresaMunicipioFilterDTO filter) {

        return repository.getAllAndFilter(filter);
    }

    @Transactional(readOnly = true)
    protected Mono<EmpresaMunicipioResponseDTO> findById(UUID id) {

        return repository.findByIdCustom(id);
    }
}
