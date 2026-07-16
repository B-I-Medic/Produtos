package com.medic.Web.service.forecast;

import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.repository.forecast.ForecastRepository;
import com.medic.Web.support.TestDataFactory;
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
    private ForecastRepository forecastRepository;

    @InjectMocks
    private ConsultaForecastService forecastService;

    @Test
    void shouldListForecastAgrupado() {

        var filter = new ForecastFilterDTO("CD", "Empresa", "SP", "Cidade", "Anvisa", "Marca", "Produto", null);
        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        when(forecastRepository.findByFilter(filter)).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(forecastService.listForecastAgrupado(filter))
                .expectNext(response)
                .verifyComplete();
    }
}
