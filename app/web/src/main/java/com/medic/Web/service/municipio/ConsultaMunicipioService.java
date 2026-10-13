package com.medic.Web.service.municipio;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.Web.mapper.municipio.MunicipioMapper;
import com.medic.Web.model.cd.CentroDistribuicaoModel;
import com.medic.Web.model.municipio.MunicipioModel;
import com.medic.Web.repository.cd.CentroDistribuicaoRepository;
import com.medic.Web.repository.cd.MunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ConsultaMunicipioService {

    private final MunicipioRepository repository;
    private final MunicipioMapper mapper;
    private final CentroDistribuicaoRepository centroDistribuicaoRepository;
    private final CentroDistribuicaoMapper centroDistribuicaoMapper;

    public ConsultaMunicipioService(MunicipioRepository repository,
                                    MunicipioMapper mapper,
                                    CentroDistribuicaoRepository centroDistribuicaoRepository,
                                    CentroDistribuicaoMapper centroDistribuicaoMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.centroDistribuicaoRepository = centroDistribuicaoRepository;
        this.centroDistribuicaoMapper = centroDistribuicaoMapper;
    }

    @Transactional(readOnly = true)
    public Flux<MunicipioResponseDTO> listMunicipios(MunicipioFilterDTO filter) {

        return repository.findByFiltro(filter)
                .flatMap(municipio -> Mono.justOrEmpty(municipio.getIdCentroDistribuicao())
                        .flatMap(centroDistribuicaoRepository::findById)
                        .map(centroDistribuicao -> centroDistribuicaoMapper.toDTO(centroDistribuicao))
                        .map(centroDistribuicao -> mapper.toDTO(municipio, centroDistribuicao))
                        .switchIfEmpty(Mono.fromSupplier(() -> mapper.toDTO(municipio))));
    }
}
