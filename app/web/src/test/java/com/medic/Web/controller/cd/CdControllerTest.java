package com.medic.Web.controller.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.cd.ManutencaoCDService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CdControllerTest {

    private final ManutencaoCDService cdService = mock(ManutencaoCDService.class);
    private WebTestClient cdClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        cdClient = WebTestClient.bindToController(new CdController(cdService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldListCds() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        when(cdService.listCDs()).thenReturn(Flux.fromIterable(List.of(response)));

        cdClient.get().uri("/centro-distribuicao/get")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CentroDistribuicaoResponseDTO.class)
                .hasSize(1)
                .contains(response);
        verify(cdService).listCDs();
    }

    @Test
    void shouldSaveCd() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD");
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
        var dto = new CentroDistribuicaoRequestDTO("CD");
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
