package com.medic.web.service.forecast;

import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.forecast.ForecastRepositoryCustom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class ConsultaForecastService {

    private final ForecastRepositoryCustom repository;

    public ConsultaForecastService(ForecastRepositoryCustom repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Mono<PaginaResponseDTO<ForecastAgrupadoResponseDTO>> listForecastAgrupado(ForecastFilterDTO filter) {
        int tamanho = Integer.parseInt(filter.getTamanhoPagina());
        return Mono.zip(repository.findByFilter(filter).collectList(), repository.countByFilter(filter))
                .map(resultado -> new PaginaResponseDTO<>(resultado.getT1(), Math.toIntExact(resultado.getT2()),
                        (int) Math.ceil((double) resultado.getT2() / tamanho)));
    }
}
