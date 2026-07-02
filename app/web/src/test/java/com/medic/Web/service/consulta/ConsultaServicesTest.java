package com.medic.Web.service.consulta;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.mapper.municipio.MunicipioMapper;
import com.medic.Web.mapper.necessidade.NecessidadeMapper;
import com.medic.Web.model.municipio.MunicipioModel;
import com.medic.Web.model.necessidade.NecessidadeModel;
import com.medic.Web.repository.cd.MunicipioRepository;
import com.medic.Web.repository.necessidade.NecessidadeRepository;
import com.medic.Web.service.muncipio.ConsultaMunicipioService;
import com.medic.Web.service.necessidade.ConsultaNecessidadeService;
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
class ConsultaServicesTest {

    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private MunicipioMapper municipioMapper;
    @Mock
    private NecessidadeRepository necessidadeRepository;
    @Mock
    private NecessidadeMapper necessidadeMapper;

    @InjectMocks
    private ConsultaMunicipioService municipioService;
    @InjectMocks
    private ConsultaNecessidadeService necessidadeService;

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

    @Test
    void shouldListNecessidades() {

        NecessidadeModel model = TestDataFactory.necessidadeModel();
        var response = TestDataFactory.necessidadeResponseDTO();
        when(necessidadeRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(necessidadeMapper.toDTO(model)).thenReturn(response);

        StepVerifier.create(necessidadeService.listNecessidades())
                .expectNext(response)
                .verifyComplete();
    }
}
