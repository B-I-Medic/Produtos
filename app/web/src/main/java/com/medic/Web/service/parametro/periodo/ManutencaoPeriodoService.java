package com.medic.Web.service.parametro.periodo;

import com.medic.Web.dto.parametro.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.parametro.periodo.PeriodoResponseDTO;
import com.medic.Web.mapper.parametro.periodo.PeriodoMapper;
import com.medic.Web.repository.parametro.PeriodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoPeriodoService {

    private final PeriodoRepository repository;
    private final PeriodoMapper mapper;

    public ManutencaoPeriodoService(PeriodoRepository repository,
                                    PeriodoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Mono<PeriodoResponseDTO> definirPeriodo(UUID periodoId,
                                                   PeriodoRequestDTO dto,
                                                   UUID userId) {

        return repository.findById(periodoId)
                .map(periodo -> mapper.map(periodo, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Flux<PeriodoResponseDTO> listPeriods() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
