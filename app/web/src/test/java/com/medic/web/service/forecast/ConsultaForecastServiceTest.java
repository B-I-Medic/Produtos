package com.medic.web.service.forecast;

import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.repository.forecast.ForecastRepositoryCustom;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaForecastServiceTest {

    @Mock
    private ForecastRepositoryCustom forecastRepository;

    @InjectMocks
    private ConsultaForecastService forecastService;

    @Test
    void shouldListForecastAgrupado() {

        var filter = new ForecastFilterDTO();
        filter.setNumeroPagina("1");
        filter.setTamanhoPagina("100");
        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        when(forecastRepository.findByFilter(filter)).thenReturn(Flux.fromIterable(List.of(response)));
        when(forecastRepository.countByFilter(filter)).thenReturn(reactor.core.publisher.Mono.just(1L));

        StepVerifier.create(forecastService.listForecastAgrupado(filter))
                .assertNext(page -> {
                    org.junit.jupiter.api.Assertions.assertEquals(List.of(response), page.pagina());
                    org.junit.jupiter.api.Assertions.assertEquals(1, page.quantidadeRegistros());
                })
                .verifyComplete();
    }
}
