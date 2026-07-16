package com.medic.Web.repository.forecast;

import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ForecastRepositoryCustom {

    Flux<ForecastAgrupadoResponseDTO> findByFilter(ForecastFilterDTO filter);
}
