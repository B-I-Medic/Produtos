package com.medic.web.controller.forecast;

import com.medic.web.dto.forecast.AgrupamentosPadrao;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.service.forecast.ConsultaForecastService;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForecastControllerTest {

    @Test
    void shouldHandleForecastEndpoint() {

        ConsultaForecastService service = mock(ConsultaForecastService.class);
        var response = TestDataFactory.forecastAgrupadoResponseDTO();

        when(service.listForecastAgrupado(any())).thenReturn(reactor.core.publisher.Mono.just(new PaginaResponseDTO<>(List.of(response), 1, 1)));

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
                        .queryParam("numeroPagina", "1")
                        .queryParam("tamanhoPagina", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.pagina[0].produto").isEqualTo(response.produto())
                .jsonPath("$.quantidadeRegistros").isEqualTo(1);
    }

    @Test
    void shouldRequirePaginationParameters() {
        WebTestClient.bindToController(new ForecastController(mock(ConsultaForecastService.class)))
                .build()
                .get().uri("/forecast/get")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldListAvailableGroupingOptions() {

        WebTestClient.bindToController(new ForecastController(mock(ConsultaForecastService.class)))
                .build()
                .get().uri("/forecast/agrupamentos-disponiveis/get")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(AgrupamentosPadrao.class)
                .hasSize(AgrupamentosPadrao.values().length);
    }
}
