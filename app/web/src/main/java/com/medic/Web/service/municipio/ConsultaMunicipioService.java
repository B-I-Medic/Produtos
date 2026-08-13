package com.medic.Web.service.municipio;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.mapper.municipio.MunicipioMapper;
import com.medic.Web.repository.cd.MunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaMunicipioService {

    private final MunicipioRepository repository;
    private final MunicipioMapper mapper;

    public ConsultaMunicipioService(MunicipioRepository repository,
                                    MunicipioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Flux<MunicipioResponseDTO> listMunicipios(MunicipioFilterDTO filter) {

        return repository.findByFiltro(filter)
                .map(mapper::toDTO);
    }
}
