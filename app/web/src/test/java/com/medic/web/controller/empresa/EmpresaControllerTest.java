package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.model.empresa.Viman;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.estoque.ConsultaEstoqueSegregadoService;
import com.medic.web.service.empresa.ConsultaEmpresaService;
import com.medic.web.service.empresa.ManutencaoEmpresaService;
import com.medic.web.support.FixedAuthenticationPrincipalResolver;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

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
    private final ConsultaEstoqueSegregadoService consultaEstoqueSegregadoService = mock(ConsultaEstoqueSegregadoService.class);
    private WebTestClient empresaClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        empresaClient = WebTestClient.bindToController(new EmpresaController(empresaService, consultaEmpresaService, consultaEstoqueSegregadoService))
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
                .uri("/empresa/get/paginado?numeroPagina=1&tamanhoPagina=10&descricao=Empresa&codigo=001&viman=UFX"
                        + "&possuiEstoqueInterno=true&possuiEstoqueSegregado=false&possuiVp=true"
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
                .uri("/empresa/get/paginado?numeroPagina=1&tamanhoPagina=10&ordenarPor=senha")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(consultaEmpresaService);
    }

    @Test
    void shouldReturnFilteredPaginatedSegregatedStock() {

        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        when(consultaEstoqueSegregadoService.getPage(org.mockito.ArgumentMatchers.eq(empresaId),
                org.mockito.ArgumentMatchers.any(EstoqueSegregadoPaginaDTO.class)))
                .thenReturn(Mono.just(new PaginaResponseDTO<>(List.of(response), 1, 1)));

        empresaClient.get()
                .uri("/empresa/{empresaId}/estoque-segregado/get/paginado?numeroPagina=1&tamanhoPagina=10"
                        + "&descricao=Estoque&municipio=" + municipioId + "&ordenarPor=municipio&direcaoOrdenacao=DESC", empresaId)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.quantidadeRegistros").isEqualTo(1);
        var filtro = org.mockito.ArgumentCaptor.forClass(EstoqueSegregadoPaginaDTO.class);
        verify(consultaEstoqueSegregadoService).getPage(org.mockito.ArgumentMatchers.eq(empresaId), filtro.capture());
        assertEquals("Estoque", filtro.getValue().getDescricao());
        assertEquals(municipioId, filtro.getValue().getMunicipio());
        assertEquals("municipio", filtro.getValue().getOrdenarPor());
        assertEquals("DESC", filtro.getValue().getDirecaoOrdenacao());
    }

    @Test
    void shouldRejectInvalidCompanyIdForSegregatedStock() {

        empresaClient.get()
                .uri("/empresa/not-a-uuid/estoque-segregado/get/paginado?numeroPagina=1&tamanhoPagina=10")
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(consultaEstoqueSegregadoService);
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
