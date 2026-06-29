package com.medic.Web.controller.cd;

import com.medic.Web.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.cd.ManutencaoCDService;
import com.medic.Web.service.cd.ManutencaoCdEmpresaMunicipioService;
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
import static org.mockito.Mockito.when;

class CdControllerTest {

    private final ManutencaoCDService cdService = mock(ManutencaoCDService.class);
    private final ManutencaoCdEmpresaMunicipioService vinculoService = mock(ManutencaoCdEmpresaMunicipioService.class);
    private WebTestClient cdClient;
    private WebTestClient vinculoClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        cdClient = WebTestClient.bindToController(new CdController(cdService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
        vinculoClient = WebTestClient.bindToController(new CdEmpresaMunicipioController(vinculoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldHandleCdEndpoints() {

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD");
        UUID id = UUID.randomUUID();
        when(cdService.listCDs()).thenReturn(Flux.fromIterable(List.of(response)));
        when(cdService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(cdService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(cdService.delete(id)).thenReturn(Mono.empty());

        cdClient.get().uri("/centro-distribuicao/get").exchange().expectStatus().isOk();
        cdClient.post().uri("/centro-distribuicao/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        cdClient.put().uri("/centro-distribuicao/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        cdClient.delete().uri("/centro-distribuicao/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleCdEmpresaMunicipioEndpoints() {

        var response = TestDataFactory.cdEmpresaMunipioResponseDTO();
        var dto = new CdEmpresaMunipioRequestDTO(response.idCd(), response.idEmpresaMunicipio());

        when(vinculoService.listCdEmpresaMunicipio()).thenReturn(Flux.fromIterable(List.of(response)));
        when(vinculoService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(vinculoService.delete(response.id())).thenReturn(Mono.empty());

        vinculoClient.get().uri("/centro-distribuicao/empresa-municipio/get").exchange().expectStatus().isOk();
        vinculoClient.post().uri("/centro-distribuicao/empresa-municipio/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        vinculoClient.delete().uri("/centro-distribuicao/empresa-municipio/delete/" + response.id()).exchange().expectStatus().isOk();
    }
}
