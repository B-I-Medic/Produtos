package com.medic.web.service.municipio;

import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.web.mapper.municipio.MunicipioMapper;
import com.medic.web.model.cd.CentroDistribuicaoModel;
import com.medic.web.model.municipio.MunicipioModel;
import com.medic.web.repository.cd.CentroDistribuicaoRepository;
import com.medic.web.repository.cd.MunicipioRepository;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaMunicipioServiceTest {

    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private MunicipioMapper municipioMapper;
    @Mock
    private CentroDistribuicaoRepository centroDistribuicaoRepository;
    @Mock
    private CentroDistribuicaoMapper centroDistribuicaoMapper;

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

    @Test
    void shouldListMunicipioWithCentroDistribuicao() {

        MunicipioModel municipio = TestDataFactory.municipioModel();
        CentroDistribuicaoModel centroDistribuicao = TestDataFactory.centroDistribuicaoModel();
        municipio.setIdCentroDistribuicao(centroDistribuicao.getId());
        var filtro = new MunicipioFilterDTO("Cidade", "SP");
        var centroDistribuicaoResponse = TestDataFactory.centroDistribuicaoResponseDTO();
        var response = new MunicipioResponseDTO(
                municipio.getId(),
                municipio.getDescricao(),
                municipio.getCodIbge(),
                municipio.getEstado(),
                centroDistribuicaoResponse
        );

        when(municipioRepository.findByFiltro(filtro)).thenReturn(Flux.just(municipio));
        when(centroDistribuicaoRepository.findById(centroDistribuicao.getId()))
                .thenReturn(Mono.just(centroDistribuicao));
        when(centroDistribuicaoMapper.toDTO(centroDistribuicao)).thenReturn(centroDistribuicaoResponse);
        when(municipioMapper.toDTO(municipio, centroDistribuicaoResponse)).thenReturn(response);

        StepVerifier.create(municipioService.listMunicipios(filtro))
                .expectNext(response)
                .verifyComplete();
    }
}
