package com.medic.Web.service.forecast;

import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.Web.repository.forecast.ForecastRepositoryCustomImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaForecastService {

    private final ForecastRepositoryCustomImpl repository;

    public ConsultaForecastService(ForecastRepositoryCustomImpl repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<ForecastAgrupadoResponseDTO> listForecastAgrupado(ForecastFilterDTO filter) {

        return repository.findByFilter(filter);
    }
}
