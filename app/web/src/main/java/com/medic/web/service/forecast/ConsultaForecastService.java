package com.medic.web.service.forecast;

import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.web.repository.forecast.ForecastRepositoryCustom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaForecastService {

    private final ForecastRepositoryCustom repository;

    public ConsultaForecastService(ForecastRepositoryCustom repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<ForecastAgrupadoResponseDTO> listForecastAgrupado(ForecastFilterDTO filter) {

        return repository.findByFilter(filter);
    }
}
