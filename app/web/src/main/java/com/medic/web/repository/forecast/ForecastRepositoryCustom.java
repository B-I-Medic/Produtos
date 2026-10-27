package com.medic.web.repository.forecast;

import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ForecastRepositoryCustom {

    Flux<ForecastAgrupadoResponseDTO> findByFilter(ForecastFilterDTO filter);
    Mono<Long> countByFilter(ForecastFilterDTO filter);
}
