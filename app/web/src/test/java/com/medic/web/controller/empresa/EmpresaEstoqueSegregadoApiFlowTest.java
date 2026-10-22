package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.web.service.config.estoque.ConsultaEstoqueSegregadoService;
import com.medic.web.service.empresa.ConsultaEmpresaService;
import com.medic.web.service.empresa.ManutencaoEmpresaService;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpresaEstoqueSegregadoApiFlowTest {

    @Test
    void shouldReturnPagedStockThroughControllerServiceAndRepository() {
        UUID empresaId = UUID.randomUUID();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        EstoqueSegregadoRepository repository = mock(EstoqueSegregadoRepository.class);
        when(repository.findPageByEmpresaAndFilter(any(), any(EstoqueSegregadoPaginaDTO.class), any(Long.class), any(Long.class)))
                .thenReturn(Flux.just(response));
        when(repository.countByEmpresaAndFilter(any(), any(EstoqueSegregadoPaginaDTO.class))).thenReturn(Mono.just(1L));
        var consultaEstoqueService = new ConsultaEstoqueSegregadoService(repository);
        var controller = new EmpresaController(mock(ManutencaoEmpresaService.class),
                mock(ConsultaEmpresaService.class), consultaEstoqueService);
        var client = WebTestClient.bindToController(controller).build();

        client.get()
                .uri("/empresa/{empresaId}/estoque-segregado/get/paginado?numeroPagina=1&tamanhoPagina=10"
                        + "&descricao=Estoque&ordenarPor=municipio", empresaId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pagina[0].id").isEqualTo(response.id().toString())
                .jsonPath("$.quantidadeRegistros").isEqualTo(1)
                .jsonPath("$.quantidadePaginas").isEqualTo(1);

        var filtro = org.mockito.ArgumentCaptor.forClass(EstoqueSegregadoPaginaDTO.class);
        verify(repository).findPageByEmpresaAndFilter(org.mockito.ArgumentMatchers.eq(empresaId),
                filtro.capture(), org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq(0L));
        verify(repository).countByEmpresaAndFilter(org.mockito.ArgumentMatchers.eq(empresaId), any());
        org.junit.jupiter.api.Assertions.assertEquals("Estoque", filtro.getValue().getDescricao());
        org.junit.jupiter.api.Assertions.assertEquals("municipio", filtro.getValue().getOrdenarPor());
    }
}
