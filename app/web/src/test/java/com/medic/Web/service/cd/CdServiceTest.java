package com.medic.Web.service.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.Web.model.cd.CentroDistribuicaoModel;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Web.repository.cd.CentroDistribuicaoRepository;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CdServiceTest {

    @Mock
    private CentroDistribuicaoRepository centroDistribuicaoRepository;
    @Mock
    private CentroDistribuicaoMapper centroDistribuicaoMapper;
    @Mock
    private CdEmpresaMunipioRepository cdEmpresaMunipioRepository;

    @InjectMocks
    private ManutencaoCDService manutencaoCDService;
    @InjectMocks
    private ManutencaoCdEmpresaMunicipioService manutencaoCdEmpresaMunicipioService;

    @Test
    void shouldCrudCentroDistribuicao() {

        CentroDistribuicaoModel model = TestDataFactory.centroDistribuicaoModel();

        var response = TestDataFactory.centroDistribuicaoResponseDTO();
        var dto = new CentroDistribuicaoRequestDTO("CD");

        when(centroDistribuicaoMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(centroDistribuicaoMapper.toDTO(model)).thenReturn(response);

        when(centroDistribuicaoRepository.save(model)).thenReturn(Mono.just(model));
        when(centroDistribuicaoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(centroDistribuicaoRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(centroDistribuicaoRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(manutencaoCDService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(manutencaoCDService.update(model.getId(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(manutencaoCDService.listCDs()).expectNext(response).verifyComplete();
        StepVerifier.create(manutencaoCDService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldListCdEmpresaMunicipio() {

        var consultaResponse = TestDataFactory.cdEmpresaMunipioConsultaResponseDTO();
        UUID cdId = UUID.randomUUID();

        when(cdEmpresaMunipioRepository.findByFiltro(cdId, null))
                .thenReturn(Flux.just(consultaResponse));

        StepVerifier.create(manutencaoCdEmpresaMunicipioService.listCdEmpresaMunicipio(cdId, null))
                .expectNext(consultaResponse)
                .verifyComplete();
    }
}
