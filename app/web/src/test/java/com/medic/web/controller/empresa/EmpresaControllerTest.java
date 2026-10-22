package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.model.empresa.Viman;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.web.service.empresa.ConsultaEmpresaService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmpresaControllerTest {

    private final ManutencaoEmpresaService empresaService = mock(ManutencaoEmpresaService.class);
    private final ConsultaEmpresaService consultaEmpresaService = mock(ConsultaEmpresaService.class);
    private final ManutencaoEstoqueSegregadoService estoqueSegregadoService = mock(ManutencaoEstoqueSegregadoService.class);
    private WebTestClient empresaClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        empresaClient = WebTestClient.bindToController(new EmpresaController(empresaService, consultaEmpresaService, estoqueSegregadoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldGetFilteredSortedEmpresaPage() {

        var response = TestDataFactory.empresaResponseDTO();
        var page = new PaginaResponseDTO<>(List.of(response), 1, 1);
        UUID municipioId = UUID.randomUUID();
        when(consultaEmpresaService.getPage(org.mockito.ArgumentMatchers.any(EmpresaPaginaDTO.class)))
                .thenReturn(Mono.just(page));

        empresaClient.get()
                .uri("/empresa/get?numeroPagina=1&tamanhoPagina=10&descricao=Empresa&codigo=001&viman=UFX"
                        + "&possui_estoque_interno=true&possui_estoque_segregado=false&possui_vp=true"
                        + "&municipio=" + municipioId + "&ordenarPor=municipio&direcaoOrdenacao=DESC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.quantidadeRegistros").isEqualTo(1);

        var filtro = org.mockito.ArgumentCaptor.forClass(EmpresaPaginaDTO.class);
        verify(consultaEmpresaService).getPage(filtro.capture());
        assertEquals("Empresa", filtro.getValue().getDescricao());
        assertEquals("001", filtro.getValue().getCodigo());
        assertEquals(Viman.UFX, filtro.getValue().getViman());
        assertEquals(true, filtro.getValue().getPossuiEstoqueInterno());
        assertEquals(false, filtro.getValue().getPossuiEstoqueSegregado());
        assertEquals(true, filtro.getValue().getPossuiVp());
        assertEquals(municipioId, filtro.getValue().getMunicipio());
        assertEquals("municipio", filtro.getValue().getOrdenarPor());
        assertEquals("DESC", filtro.getValue().getDirecaoOrdenacao());
    }

    @Test
    void shouldRejectUnsupportedEmpresaSortField() {

        empresaClient.get()
                .uri("/empresa/get?numeroPagina=1&tamanhoPagina=10&ordenarPor=senha")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(consultaEmpresaService);
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
