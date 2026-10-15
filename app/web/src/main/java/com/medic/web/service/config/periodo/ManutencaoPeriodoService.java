package com.medic.web.service.config.periodo;

import com.medic.web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.web.mapper.config.periodo.PeriodoMapper;
import com.medic.web.repository.config.PeriodoRepository;
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
