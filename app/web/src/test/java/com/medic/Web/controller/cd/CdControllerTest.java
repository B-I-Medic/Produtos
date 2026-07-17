package com.medic.Web.controller.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.cd.ManutencaoCDService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.mock;
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
    void shouldHandleCdEndpoints() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD");
        UUID id = UUID.randomUUID();
        when(cdService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(cdService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(cdService.delete(id)).thenReturn(Mono.empty());

        cdClient.post().uri("/centro-distribuicao/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        cdClient.put().uri("/centro-distribuicao/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        cdClient.delete().uri("/centro-distribuicao/delete/" + id).exchange().expectStatus().isOk();
    }
}
