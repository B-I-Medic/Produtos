package com.medic.Produtos.controller.consulta;

import com.medic.Produtos.controller.municipio.MunicipioController;
import com.medic.Produtos.controller.necessidade.NecessidadeController;
import com.medic.Produtos.service.muncipio.ConsultaMunicipioService;
import com.medic.Produtos.service.necessidade.ConsultaNecessidadeService;
import com.medic.Produtos.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultaControllerTest {

    @Test
    void shouldHandleMunicipioAndNecessidadeEndpoints() {

        ConsultaMunicipioService municipioService = mock(ConsultaMunicipioService.class);
        ConsultaNecessidadeService necessidadeService = mock(ConsultaNecessidadeService.class);
        when(municipioService.listMunicipios()).thenReturn(Flux.fromIterable(List.of(TestDataFactory.municipioResponseDTO())));
        when(necessidadeService.listNecessidades()).thenReturn(Flux.fromIterable(List.of(TestDataFactory.necessidadeResponseDTO())));

        WebTestClient.bindToController(new MunicipioController(municipioService))
                .build()
                .get().uri("/municipio/get")
                .exchange()
                .expectStatus().isOk();

        WebTestClient.bindToController(new NecessidadeController(necessidadeService))
                .build()
                .get().uri("/necessidade/get")
                .exchange()
                .expectStatus().isOk();
    }
}
