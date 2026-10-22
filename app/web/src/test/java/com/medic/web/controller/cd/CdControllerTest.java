package com.medic.web.controller.cd;

import com.medic.web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.cd.ManutencaoCDService;
import com.medic.web.service.cd.ConsultaCDService;
import com.medic.web.support.FixedAuthenticationPrincipalResolver;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CdControllerTest {

    private final ManutencaoCDService cdService = mock(ManutencaoCDService.class);
    private final ConsultaCDService consultaCDService = mock(ConsultaCDService.class);
    private WebTestClient cdClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        cdClient = WebTestClient.bindToController(new CdController(cdService, consultaCDService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldGetFilteredAndSortedCdPage() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        UUID municipioId = UUID.randomUUID();
        when(consultaCDService.getPage(org.mockito.ArgumentMatchers.any(CentroDistribuicaoPaginaDTO.class)))
                .thenReturn(Mono.just(new PaginaResponseDTO<>(java.util.List.of(response), 1, 1)));

        cdClient.get().uri("/centro-distribuicao/get/paginado?numeroPagina=1&tamanhoPagina=10&descricao=Central"
                        + "&municipio=" + municipioId + "&ordenarPor=municipio&direcaoOrdenacao=DESC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.quantidadeRegistros").isEqualTo(1);
        var filtro = org.mockito.ArgumentCaptor.forClass(CentroDistribuicaoPaginaDTO.class);
        verify(consultaCDService).getPage(filtro.capture());
        org.junit.jupiter.api.Assertions.assertEquals("Central", filtro.getValue().getDescricao());
        org.junit.jupiter.api.Assertions.assertEquals(municipioId, filtro.getValue().getMunicipio());
        org.junit.jupiter.api.Assertions.assertEquals("municipio", filtro.getValue().getOrdenarPor());
        org.junit.jupiter.api.Assertions.assertEquals("DESC", filtro.getValue().getDirecaoOrdenacao());
    }

    @Test
    void shouldRejectUnsupportedCdSortField() {
        cdClient.get().uri("/centro-distribuicao/get/paginado?numeroPagina=1&tamanhoPagina=10&ordenarPor=id")
                .exchange().expectStatus().isBadRequest();
        org.mockito.Mockito.verifyNoInteractions(consultaCDService);
    }

    @Test
    void shouldSaveCd() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD", UUID.randomUUID());
        when(cdService.save(dto, user.getId())).thenReturn(Mono.just(response));

        cdClient.post()
                .uri("/centro-distribuicao/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(cdService).save(dto, user.getId());
    }

    @Test
    void shouldUpdateCd() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD", UUID.randomUUID());
        UUID id = UUID.randomUUID();
        when(cdService.update(id, dto, user.getId())).thenReturn(Mono.just(response));

        cdClient.put()
                .uri("/centro-distribuicao/update/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(cdService).update(id, dto, user.getId());
    }

    @Test
    void shouldDeleteCd() {

        UUID id = UUID.randomUUID();
        when(cdService.delete(id)).thenReturn(Mono.empty());

        cdClient.delete()
                .uri("/centro-distribuicao/delete/" + id)
                .exchange()
                .expectStatus().isOk();

        verify(cdService).delete(id);
    }
}
