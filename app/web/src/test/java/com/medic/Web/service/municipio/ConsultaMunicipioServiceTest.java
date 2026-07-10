package com.medic.Web.service.municipio;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.mapper.municipio.MunicipioMapper;
import com.medic.Web.model.municipio.MunicipioModel;
import com.medic.Web.repository.cd.MunicipioRepository;
import com.medic.Web.service.muncipio.ConsultaMunicipioService;
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
class ConsultaMunicipioServiceTest {

    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private MunicipioMapper municipioMapper;

    @InjectMocks
    private ConsultaMunicipioService municipioService;

    @Test
    void shouldListMunicipios() {

        MunicipioModel model = TestDataFactory.municipioModel();
        var response = TestDataFactory.municipioResponseDTO();
        var filter = new MunicipioFilterDTO("Cidade", "SP");
        when(municipioRepository.findByFiltro(filter)).thenReturn(Flux.fromIterable(List.of(model)));
        when(municipioMapper.toDTO(model)).thenReturn(response);

        StepVerifier.create(municipioService.listMunicipios(filter))
                .expectNext(response)
                .verifyComplete();
    }
}
