package com.medic.web.service.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.repository.municipio.MunicipioRepository;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaMunicipioServiceTest {

    @Mock
    private MunicipioRepository municipioRepository;

    @InjectMocks
    private ConsultaMunicipioService municipioService;

    @Test
    void shouldPageMunicipiosAndBuildMetadata() {

        var filter = new MunicipioPaginaDTO();
        filter.setNumeroPagina("2");
        filter.setTamanhoPagina("10");
        UUID cdId = UUID.randomUUID();
        filter.setCentroDistribuicao(cdId);
        var response = TestDataFactory.municipioResponseDTO();
        when(municipioRepository.findPageByFilter(filter, 10, 10)).thenReturn(Flux.just(response));
        when(municipioRepository.countByFilter(filter)).thenReturn(Mono.just(11L));

        StepVerifier.create(municipioService.getPage(filter))
                .assertNext(page -> {
                    assertEquals(java.util.List.of(response), page.pagina());
                    assertEquals(11, page.quantidadeRegistros());
                    assertEquals(2, page.quantidadePaginas());
                })
                .verifyComplete();
        verify(municipioRepository).findPageByFilter(filter, 10, 10);
        verify(municipioRepository).countByFilter(filter);
    }
}
