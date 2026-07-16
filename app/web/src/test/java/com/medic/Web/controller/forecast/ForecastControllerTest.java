package com.medic.Web.controller.forecast;

import com.medic.Web.dto.forecast.AgrupamentosPadrao;
import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.Web.service.forecast.ConsultaForecastService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForecastControllerTest {

    @Test
    void shouldHandleForecastEndpoint() {

        ConsultaForecastService service = mock(ConsultaForecastService.class);
        var response = TestDataFactory.forecastAgrupadoResponseDTO();

        when(service.listForecastAgrupado(any())).thenReturn(Flux.fromIterable(List.of(response)));

        WebTestClient.bindToController(new ForecastController(service))
                .build()
                .get().uri(uriBuilder -> uriBuilder
                        .path("/forecast/get")
                        .queryParam("centroDistribuicao", "CD")
                        .queryParam("empresa", "Empresa")
                        .queryParam("estado", "SP")
                        .queryParam("municipio", "Cidade")
                        .queryParam("anvisa", "Anvisa")
                        .queryParam("produto", "Produto")
                        .queryParam("marca", "Marca")
                        .queryParam("groupBy", "CENTRO_DISTRIBUICAO")
                        .queryParam("groupBy", "EMPRESA")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ForecastAgrupadoResponseDTO.class)
                .hasSize(1)
                .contains(response);
    }

    @Test
    void shouldListAvailableGroupingOptions() {

        WebTestClient.bindToController(new ForecastController(mock(ConsultaForecastService.class)))
                .build()
                .get().uri("/forecast/agrupamentos-disponiveis/get")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AgrupamentosPadrao.class)
                .hasSize(AgrupamentosPadrao.values().length);
    }
}
