package com.medic.Produtos.service.empresa;

import com.medic.Produtos.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Produtos.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Produtos.mapper.empresa.EmpresaMunicipioMapper;
import com.medic.Produtos.model.cd.CdEmpresaMunipioModel;
import com.medic.Produtos.model.empresa.EmpresaMunicipioModel;
import com.medic.Produtos.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Produtos.repository.empresa.EmpresaMunicipioRepository;
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
    public Mono<EmpresaMunicipioResponseDTO> save(UUID cdId,
                                                  EmpresaMunicipioRequestDTO dto,
                                                  UUID userId) {

        return Mono.just(new EmpresaMunicipioModel())
                .map(empresaMunicipio -> mapper.toEntity(empresaMunicipio, dto, userId))
                .flatMap(repository::save)
                .flatMap(empresaMunicipio -> cdEmpresaMunipioRepository
                        .save(
                                cdEmpresaMunipioMapper.toEntity(
                                        new CdEmpresaMunipioModel(),
                                        new CdEmpresaMunipioRequestDTO(cdId, empresaMunicipio.getId()),
                                        userId
                                )
                        )
                        .thenReturn(empresaMunicipio))
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID empresaMunicipioId) {

        return repository.deleteById(empresaMunicipioId);
    }

    @Transactional(readOnly = true)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresasMunicipio() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
