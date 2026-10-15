package com.medic.web.controller.municipio;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.service.municipio.ConsultaMunicipioService;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MunicipioControllerTest {

    @Test
    void shouldHandleMunicipioEndpoint() {

        ConsultaMunicipioService municipioService = mock(ConsultaMunicipioService.class);
        var municipioResponse = TestDataFactory.municipioResponseDTO();
        var filter = new MunicipioFilterDTO("Cidade", "SP", "CD Central");
        when(municipioService.listMunicipios(filter)).thenReturn(Flux.fromIterable(List.of(municipioResponse)));

        WebTestClient.bindToController(new MunicipioController(municipioService))
                .build()
                .get().uri(uriBuilder -> uriBuilder
                        .path("/municipio/get")
                        .queryParam("descricao", "Cidade")
                        .queryParam("estado", "SP")
                        .queryParam("centroDistribuicao", "CD Central")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MunicipioResponseDTO.class)
                .hasSize(1)
                .contains(municipioResponse);
    }
}
