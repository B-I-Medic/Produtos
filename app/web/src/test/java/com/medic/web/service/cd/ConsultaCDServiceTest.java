package com.medic.web.service.cd;

import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.repository.cd.CentroDistribuicaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaCDServiceTest {

    @Mock
    private CentroDistribuicaoRepository repository;

    @Test
    void shouldRequestBoundedPageAndBuildMetadata() {
        var filtro = new CentroDistribuicaoPaginaDTO();
        filtro.setNumeroPagina("2");
        filtro.setTamanhoPagina("10");
        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC");
        var response = new CentroDistribuicaoResponseDTO(null, "CD", null);
        when(repository.findPageByFilter(filtro, 10, 10)).thenReturn(Flux.just(response));
        when(repository.countByFilter(filtro)).thenReturn(Mono.just(21L));

        StepVerifier.create(new ConsultaCDService(repository).getPage(filtro))
                .assertNext(page -> {
                    assertEquals(List.of(response), page.pagina());
                    assertEquals(21, page.quantidadeRegistros());
                    assertEquals(3, page.quantidadePaginas());
                })
                .verifyComplete();
        verify(repository).findPageByFilter(filtro, 10, 10);
        verify(repository).countByFilter(filtro);
    }
}
