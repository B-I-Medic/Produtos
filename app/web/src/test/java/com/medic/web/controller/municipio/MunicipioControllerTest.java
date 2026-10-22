package com.medic.web.controller.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import org.junit.jupiter.api.Assertions;
import com.medic.web.service.municipio.ConsultaMunicipioService;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MunicipioControllerTest {

    @Test
    void shouldGetFilteredSortedMunicipioPage() {

        ConsultaMunicipioService municipioService = mock(ConsultaMunicipioService.class);
        var municipioResponse = TestDataFactory.municipioResponseDTO();
        UUID centroDistribuicaoId = UUID.randomUUID();
        when(municipioService.getPage(org.mockito.ArgumentMatchers.any(MunicipioPaginaDTO.class)))
                .thenReturn(Mono.just(new PaginaResponseDTO<>(List.of(municipioResponse), 1, 1)));

        WebTestClient.bindToController(new MunicipioController(municipioService))
                .build()
                .get().uri(uriBuilder -> uriBuilder
                        .path("/municipio/get/paginado")
                        .queryParam("numeroPagina", "1")
                        .queryParam("tamanhoPagina", "10")
                        .queryParam("descricao", "Cidade")
                        .queryParam("estado", "SP")
                        .queryParam("centroDistribuicao", centroDistribuicaoId.toString())
                        .queryParam("ordenarPor", "centroDistribuicao")
                        .queryParam("direcaoOrdenacao", "DESC")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.quantidadeRegistros").isEqualTo(1);
        var filtro = org.mockito.ArgumentCaptor.forClass(MunicipioPaginaDTO.class);
        org.mockito.Mockito.verify(municipioService).getPage(filtro.capture());
        Assertions.assertEquals("Cidade", filtro.getValue().getDescricao());
        Assertions.assertEquals("SP", filtro.getValue().getEstado());
        Assertions.assertEquals(centroDistribuicaoId, filtro.getValue().getCentroDistribuicao());
        Assertions.assertEquals("centroDistribuicao", filtro.getValue().getOrdenarPor());
        Assertions.assertEquals("DESC", filtro.getValue().getDirecaoOrdenacao());
    }

    @Test
    void shouldRejectIdAsClientSortField() {
        ConsultaMunicipioService municipioService = mock(ConsultaMunicipioService.class);
        WebTestClient.bindToController(new MunicipioController(municipioService)).build()
                .get().uri("/municipio/get/paginado?numeroPagina=1&tamanhoPagina=10&ordenarPor=id")
                .exchange().expectStatus().isBadRequest();
        verifyNoInteractions(municipioService);
    }
}
