package com.medic.Web.repository.forecast;

import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import reactor.core.publisher.Flux;

public interface ForecastRepositoryCustom {

    Flux<ForecastAgrupadoResponseDTO> findByFilter(ForecastFilterDTO filter);
}
