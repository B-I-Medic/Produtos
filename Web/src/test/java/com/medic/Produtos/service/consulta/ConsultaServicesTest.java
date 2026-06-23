package com.medic.Produtos.service.consulta;

import com.medic.Produtos.mapper.municipio.MunicipioMapper;
import com.medic.Produtos.mapper.necessidade.NecessidadeMapper;
import com.medic.Produtos.model.municipio.MunicipioModel;
import com.medic.Produtos.model.necessidade.NecessidadeModel;
import com.medic.Produtos.repository.cd.MunicipioRepository;
import com.medic.Produtos.repository.necessidade.NecessidadeRepository;
import com.medic.Produtos.service.muncipio.ConsultaMunicipioService;
import com.medic.Produtos.service.necessidade.ConsultaNecessidadeService;
import com.medic.Produtos.support.TestDataFactory;
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
        when(municipioRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(municipioMapper.toDTO(model)).thenReturn(response);

        StepVerifier.create(municipioService.listMunicipios())
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
