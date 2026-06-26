package com.medic.Produtos.controller.empresa;

import com.medic.Produtos.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaRequestDTO;
import com.medic.Produtos.model.empresa.Viman;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.service.empresa.ManutencaoEmpresaMunicipioService;
import com.medic.Produtos.service.empresa.ManutencaoEmpresaService;
import com.medic.Produtos.support.FixedAuthenticationPrincipalResolver;
import com.medic.Produtos.support.TestDataFactory;
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
    void shouldHandleEmpresaEndpoints() {

        var response = TestDataFactory.empresaResponseDTO();
        var dto = new EmpresaRequestDTO("Empresa", Viman.UFX, "001", true, true, true);
        UUID id = UUID.randomUUID();
        when(empresaService.listEmpresas()).thenReturn(Flux.fromIterable(List.of(response)));
        when(empresaService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(empresaService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(empresaService.delete(id)).thenReturn(Mono.empty());

        empresaClient.get().uri("/empresa/get").exchange().expectStatus().isOk();
        empresaClient.post().uri("/empresa/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        empresaClient.put().uri("/empresa/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        empresaClient.delete().uri("/empresa/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleEmpresaMunicipioEndpoints() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        var dto = new EmpresaMunicipioRequestDTO(response.idEmpresa(), response.idMunicipio());
        UUID cdId = UUID.randomUUID();
        when(empresaMunicipioService.listEmpresasMunicipio()).thenReturn(Flux.fromIterable(List.of(response)));
        when(empresaMunicipioService.save(cdId, dto, user.getId())).thenReturn(Mono.just(response));
        when(empresaMunicipioService.delete(response.id())).thenReturn(Mono.empty());

        empresaMunicipioClient.get().uri("/empresa/municipio/get").exchange().expectStatus().isOk();
        empresaMunicipioClient.post().uri("/empresa/municipio/save/" + cdId).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        empresaMunicipioClient.delete().uri("/empresa/municipio/delete/" + response.id()).exchange().expectStatus().isOk();
    }
}
