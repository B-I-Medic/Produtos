package com.medic.web.service.municipio;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.repository.municipio.MunicipioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaMunicipioService {

    private final MunicipioRepository repository;

    public ConsultaMunicipioService(MunicipioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<MunicipioResponseDTO> listMunicipios(MunicipioFilterDTO filter) {

        return repository.findByFiltro(filter);
    }
}
