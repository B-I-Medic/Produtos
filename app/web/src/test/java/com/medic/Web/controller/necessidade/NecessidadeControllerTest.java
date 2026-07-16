package com.medic.Web.controller.necessidade;

import com.medic.Web.service.necessidade.ConsultaNecessidadeService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NecessidadeControllerTest {

    @Test
    void shouldHandleNecessidadeEndpoint() {

        ConsultaNecessidadeService service = mock(ConsultaNecessidadeService.class);
        var response = TestDataFactory.necessidadeAgrupadoResponseDTO();

        when(service.listNecessidadesAgrupadas(any())).thenReturn(Flux.fromIterable(List.of(response)));

        WebTestClient.bindToController(new NecessidadeController(service))
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
                .expectBodyList(com.medic.Web.dto.necessidade.NecessidadeAgrupadoResponseDTO.class)
                .hasSize(1)
                .contains(response);
    }

    @Test
    void shouldListAvailableGroupingOptions() {

        WebTestClient.bindToController(new NecessidadeController(mock(ConsultaNecessidadeService.class)))
                .build()
                .get().uri("/forecast/agrupamentos-disponiveis/get")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(com.medic.Web.model.necessidade.AgrupamentosPadrao.class)
                .hasSize(com.medic.Web.model.necessidade.AgrupamentosPadrao.values().length);
    }
}
