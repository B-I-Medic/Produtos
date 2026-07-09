package com.medic.Web.controller.consulta;

import com.medic.Web.controller.municipio.MunicipioController;
import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.service.muncipio.ConsultaMunicipioService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultaControllerTest {

    @Test
    void shouldHandleMunicipioEndpoint() {

        ConsultaMunicipioService municipioService = mock(ConsultaMunicipioService.class);
        var municipioResponse = TestDataFactory.municipioResponseDTO();
        var filter = new MunicipioFilterDTO("Cidade", "SP");
        when(municipioService.listMunicipios(filter)).thenReturn(Flux.fromIterable(List.of(municipioResponse)));

        WebTestClient.bindToController(new MunicipioController(municipioService))
                .build()
                .get().uri(uriBuilder -> uriBuilder
                        .path("/municipio/get")
                        .queryParam("descricao", "Cidade")
                        .queryParam("estado", "SP")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MunicipioResponseDTO.class)
                .hasSize(1)
                .contains(municipioResponse);
    }
}
