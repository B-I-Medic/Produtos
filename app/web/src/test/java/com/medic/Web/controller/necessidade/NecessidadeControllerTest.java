package com.medic.Web.controller.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.service.necessidade.ConsultaNecessidadeService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NecessidadeControllerTest {

    @Test
    void shouldHandleNecessidadeEndpoint() {

        ConsultaNecessidadeService service = mock(ConsultaNecessidadeService.class);
        var filter = new NecessidadeFilterDTO("CD", "Empresa", "Cidade", "Produto", "Marca");
        var response = TestDataFactory.necessidadeAgrupadoPorCDResponseDTO();

        when(service.listNecessidadesAgrupadoPorCD(filter)).thenReturn(Flux.fromIterable(List.of(response)));

        WebTestClient.bindToController(new NecessidadeController(service))
                .build()
                .get().uri(uriBuilder -> uriBuilder
                        .path("/necessidade/get/agrupado-por-cd")
                        .queryParam("centroDistribuicao", "CD")
                        .queryParam("empresa", "Empresa")
                        .queryParam("municipio", "Cidade")
                        .queryParam("produto", "Produto")
                        .queryParam("marca", "Marca")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(com.medic.Web.dto.necessidade.NecessidadeAgrupadoPorCDResponseDTO.class)
                .hasSize(1)
                .contains(response);
    }
}
