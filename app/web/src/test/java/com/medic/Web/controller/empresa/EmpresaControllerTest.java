package com.medic.Web.controller.empresa;

import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.model.usuario.UsuarioModel;
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
    private WebTestClient empresaClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        empresaClient = WebTestClient.bindToController(new EmpresaController(empresaService))
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
    void shouldSaveEmpresa() {

        var response = TestDataFactory.empresaResponseDTO();
        var dto = new EmpresaRequestDTO("Empresa", UUID.randomUUID(), Viman.UFX, "001", true, true, true);
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
        var dto = new EmpresaRequestDTO("Empresa", UUID.randomUUID(), Viman.UFX, "001", true, true, true);
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

}
