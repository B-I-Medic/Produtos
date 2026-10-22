package com.medic.web.controller.forecast;

import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.web.dto.forecast.AgrupamentosPadrao;
import com.medic.web.service.forecast.ConsultaForecastService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    private final ConsultaForecastService service;

    public ForecastController(ConsultaForecastService service) {
        this.service = service;
    }

    @GetMapping("/get")
    public Flux<ForecastAgrupadoResponseDTO> listForecastAgrupado(@ModelAttribute ForecastFilterDTO filter) {

        return service.listForecastAgrupado(filter);
    }

    @GetMapping("/agrupamentos-disponiveis/get")
    public Flux<AgrupamentosPadrao> listAgrupamentosDisponiveis() {

        return Flux.fromArray(AgrupamentosPadrao.values());
    }
}
