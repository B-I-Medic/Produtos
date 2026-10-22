package com.medic.web.service.estoque;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaEstoqueSegregadoServiceTest {

    @Mock
    private EstoqueSegregadoRepository repository;

    @Test
    void shouldUseRequestedOffsetAndBuildPageMetadata() {
        UUID empresaId = UUID.randomUUID();
        var filtro = new EstoqueSegregadoPaginaDTO();
        filtro.setNumeroPagina("3");
        filtro.setTamanhoPagina("5");
        var response = new EstoqueSegregadoResponseDTO(null, 1, "Item", true, null, null);
        when(repository.findPageByEmpresaAndFilter(empresaId, filtro, 5, 10)).thenReturn(Flux.just(response));
        when(repository.countByEmpresaAndFilter(empresaId, filtro)).thenReturn(Mono.just(11L));

        StepVerifier.create(new com.medic.web.service.config.estoque.ConsultaEstoqueSegregadoService(repository)
                        .getPage(empresaId, filtro))
                .assertNext(page -> {
                    assertEquals(List.of(response), page.pagina());
                    assertEquals(11, page.quantidadeRegistros());
                    assertEquals(3, page.quantidadePaginas());
                })
                .verifyComplete();
        verify(repository).findPageByEmpresaAndFilter(empresaId, filtro, 5, 10);
        verify(repository).countByEmpresaAndFilter(empresaId, filtro);
    }
}
