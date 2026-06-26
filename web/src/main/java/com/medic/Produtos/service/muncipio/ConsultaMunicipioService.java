package com.medic.Produtos.service.muncipio;

import com.medic.Produtos.dto.municipio.MunicipioResponseDTO;
import com.medic.Produtos.mapper.municipio.MunicipioMapper;
import com.medic.Produtos.repository.cd.MunicipioRepository;
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
    public Flux<MunicipioResponseDTO> listMunicipios() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
