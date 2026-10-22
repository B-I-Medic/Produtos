package com.medic.web.service.empresa;

import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.model.empresa.Viman;
import com.medic.web.repository.empresa.EmpresaRepository;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaEmpresaServiceTest {

    @Mock
    private EmpresaRepository repository;

    @InjectMocks
    private ConsultaEmpresaService consultaService;

    @Test
    void shouldUseDatabasePageAndCountForFilteredRequest() {

        var response = TestDataFactory.empresaResponseDTO();
        EmpresaPaginaDTO filtro = new EmpresaPaginaDTO();
        filtro.setNumeroPagina("2");
        filtro.setTamanhoPagina("10");
        filtro.setDescricao("Empresa");
        filtro.setViman(Viman.UFX);
        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC");

        when(repository.findPageByFilter(filtro, 10, 10)).thenReturn(Flux.just(response));
        when(repository.countByFilter(filtro)).thenReturn(Mono.just(21L));

        StepVerifier.create(consultaService.getPage(filtro))
                .expectNextMatches(page -> page.pagina().size() == 1
                        && page.pagina().getFirst().equals(response)
                        && page.quantidadeRegistros() == 21
                        && page.quantidadePaginas() == 3)
                .verifyComplete();

        verify(repository).findPageByFilter(filtro, 10, 10);
        verify(repository).countByFilter(filtro);
    }

    @Test
    void shouldKeepFilteredTotalWhenPageHasNoCompanies() {

        EmpresaPaginaDTO filtro = new EmpresaPaginaDTO();
        filtro.setNumeroPagina("3");
        filtro.setTamanhoPagina("2");
        filtro.setPossuiVp(false);

        when(repository.findPageByFilter(filtro, 2, 4)).thenReturn(Flux.empty());
        when(repository.countByFilter(filtro)).thenReturn(Mono.just(5L));

        StepVerifier.create(consultaService.getPage(filtro))
                .expectNextMatches(page -> page.pagina().isEmpty()
                        && page.quantidadeRegistros() == 5
                        && page.quantidadePaginas() == 3)
                .verifyComplete();

        verify(repository).findPageByFilter(filtro, 2, 4);
        verify(repository).countByFilter(filtro);
    }
}
