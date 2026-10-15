package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.model.empresa.Viman;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.web.service.empresa.ManutencaoEmpresaService;
import com.medic.web.support.FixedAuthenticationPrincipalResolver;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmpresaControllerTest {

    private final ManutencaoEmpresaService empresaService = mock(ManutencaoEmpresaService.class);
    private final ManutencaoEstoqueSegregadoService estoqueSegregadoService = mock(ManutencaoEstoqueSegregadoService.class);
    private WebTestClient empresaClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        empresaClient = WebTestClient.bindToController(new EmpresaController(empresaService, estoqueSegregadoService))
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
    void shouldReturnSegregatedStockForCompanyId() {

        UUID empresaId = UUID.randomUUID();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        when(estoqueSegregadoService.listEstoqueSegregadoByEmpresa(empresaId)).thenReturn(Flux.just(response));

        var result = empresaClient.get()
                .uri("/empresa/{empresaId}/estoque-segregado/get", empresaId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(EstoqueSegregadoResponseDTO.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(response)
                .verifyComplete();

        verify(estoqueSegregadoService).listEstoqueSegregadoByEmpresa(empresaId);
    }

    @Test
    void shouldReturnEmptyWhenCompanyHasNoSegregatedStock() {

        UUID empresaId = UUID.randomUUID();
        when(estoqueSegregadoService.listEstoqueSegregadoByEmpresa(empresaId)).thenReturn(Flux.empty());

        var result = empresaClient.get()
                .uri("/empresa/{empresaId}/estoque-segregado/get", empresaId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(EstoqueSegregadoResponseDTO.class);

        StepVerifier.create(result.getResponseBody()).verifyComplete();

        verify(estoqueSegregadoService).listEstoqueSegregadoByEmpresa(empresaId);
    }

    @Test
    void shouldRejectInvalidCompanyIdForSegregatedStock() {

        empresaClient.get()
                .uri("/empresa/not-a-uuid/estoque-segregado/get")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(estoqueSegregadoService);
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
