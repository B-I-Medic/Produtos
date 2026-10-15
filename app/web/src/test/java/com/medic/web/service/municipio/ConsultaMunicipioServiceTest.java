package com.medic.web.service.municipio;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.repository.municipio.MunicipioRepository;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaMunicipioServiceTest {

    @Mock
    private MunicipioRepository municipioRepository;

    @InjectMocks
    private ConsultaMunicipioService municipioService;

    @Test
    void shouldReturnMunicipiosFromRepository() {

        var response = TestDataFactory.municipioResponseDTO();
        var filter = new MunicipioFilterDTO("Cidade", "SP", "CD Central");
        when(municipioRepository.findByFiltro(filter)).thenReturn(Flux.just(response));

        StepVerifier.create(municipioService.listMunicipios(filter))
                .expectNext(response)
                .verifyComplete();
    }
}
