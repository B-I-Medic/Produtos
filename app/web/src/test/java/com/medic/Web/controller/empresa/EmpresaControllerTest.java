package com.medic.Web.controller.empresa;

import com.medic.Web.controller.empresaMunicipioCd.EmpresaMunicipioController;
import com.medic.Web.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.empresa.ManutencaoEmpresaMunicipioService;
import com.medic.Web.service.empresa.ManutencaoEmpresaService;
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

class EmpresaControllerTest {

    private final ManutencaoEmpresaService empresaService = mock(ManutencaoEmpresaService.class);
    private final ManutencaoEmpresaMunicipioService empresaMunicipioService = mock(ManutencaoEmpresaMunicipioService.class);
    private WebTestClient empresaClient;
    private WebTestClient empresaMunicipioClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        empresaClient = WebTestClient.bindToController(new EmpresaController(empresaService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
        empresaMunicipioClient = WebTestClient.bindToController(new EmpresaMunicipioController(empresaMunicipioService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldListEmpresas() {

        var response = TestDataFactory.empresaResponseDTO();
        when(empresaService.listEmpresas()).thenReturn(Flux.fromIterable(List.of(response)));

        empresaClient.get().uri("/empresa/get").exchange().expectStatus().isOk();
        verify(empresaService).listEmpresas();
    }

    @Test
    void shouldListEmpresaMunicipioByEmpresa() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        UUID id = UUID.randomUUID();
        when(empresaService.listEmpresaMunicipioByIDEmpresa(id)).thenReturn(Flux.fromIterable(List.of(response)));

        empresaClient.get().uri("/empresa/" + id + "/empresa-municipio/get")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmpresaMunicipioResponseDTO.class)
                .hasSize(1)
                .contains(response);

        verify(empresaService).listEmpresaMunicipioByIDEmpresa(id);
    }

    @Test
    void shouldSaveEmpresa() {

        var response = TestDataFactory.empresaResponseDTO();
        var dto = new EmpresaRequestDTO("Empresa", Viman.UFX, "001", true, true, true);
        when(empresaService.save(dto, user.getId())).thenReturn(Mono.just(response));

        empresaClient.post()
                .uri("/empresa/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(empresaService).save(dto, user.getId());
    }

    @Test
    void shouldUpdateEmpresa() {

        var response = TestDataFactory.empresaResponseDTO();
        var dto = new EmpresaRequestDTO("Empresa", Viman.UFX, "001", true, true, true);
        UUID id = UUID.randomUUID();
        when(empresaService.update(id, dto, user.getId())).thenReturn(Mono.just(response));

        empresaClient.put()
                .uri("/empresa/update/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(empresaService).update(id, dto, user.getId());
    }

    @Test
    void shouldDeleteEmpresa() {

        UUID id = UUID.randomUUID();
        when(empresaService.delete(id)).thenReturn(Mono.empty());

        empresaClient.delete().uri("/empresa/delete/" + id).exchange().expectStatus().isOk();

        verify(empresaService).delete(id);
    }

    @Test
    void shouldListEmpresasMunicipio() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        var filter = new EmpresaMunicipioFilterDTO("Empresa", "Cidade", "SP", "CD");

        when(empresaMunicipioService.listEmpresasMunicipio(filter)).thenReturn(Flux.fromIterable(List.of(response)));

        empresaMunicipioClient.get().uri(uriBuilder -> uriBuilder
                        .path("/empresa/municipio/get")
                        .queryParam("empresa", "Empresa")
                        .queryParam("municipio", "Cidade")
                        .queryParam("estado", "SP")
                        .queryParam("centroDistribuicao", "CD")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmpresaMunicipioResponseDTO.class)
                .hasSize(1)
                .contains(response);
        verify(empresaMunicipioService).listEmpresasMunicipio(filter);
    }

    @Test
    void shouldSaveEmpresaMunicipio() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        UUID empresaId = UUID.randomUUID();
        UUID cdId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        var dto = new EmpresaMunicipioRequestDTO(empresaId, cdId, municipioId);
        when(empresaMunicipioService.save(dto, user.getId())).thenReturn(Mono.just(response));

        empresaMunicipioClient.post().uri("/empresa/municipio/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmpresaMunicipioResponseDTO.class)
                .isEqualTo(response);

        verify(empresaMunicipioService).save(dto, user.getId());
    }

    @Test
    void shouldDeleteEmpresaMunicipio() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        when(empresaMunicipioService.delete(response.id())).thenReturn(Mono.empty());

        empresaMunicipioClient.delete()
                .uri("/empresa/municipio/delete/" + response.id())
                .exchange()
                .expectStatus().isOk();

        verify(empresaMunicipioService).delete(response.id());
    }
}
