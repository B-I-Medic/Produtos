package com.medic.Web.controller.forecast;

import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.Web.dto.forecast.AgrupamentosPadrao;
import com.medic.Web.service.forecast.ConsultaForecastService;
import org.springframework.http.MediaType;
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

    @GetMapping(value = {"/get"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ForecastAgrupadoResponseDTO> listForecastAgrupado(@ModelAttribute ForecastFilterDTO filter) {

        return service.listForecastAgrupado(filter);
    }

    @GetMapping(value = {"/agrupamentos-disponiveis/get"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgrupamentosPadrao> listAgrupamentosDisponiveis() {

        return Flux.fromArray(AgrupamentosPadrao.values());
    }
}
