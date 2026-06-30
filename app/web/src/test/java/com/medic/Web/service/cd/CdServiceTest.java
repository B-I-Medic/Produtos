package com.medic.Web.service.cd;

import com.medic.Web.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Web.mapper.cd.CentroDistribuicaoMapper;
import com.medic.Web.model.cd.CdEmpresaMunipioModel;
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
    @Mock
    private CdEmpresaMunipioMapper cdEmpresaMunipioMapper;

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
    void shouldCrudCdEmpresaMunicipio() {

        CdEmpresaMunipioModel model = TestDataFactory.cdEmpresaMunipioModel();

        var response = TestDataFactory.cdEmpresaMunipioResponseDTO();
        var dto = new CdEmpresaMunipioRequestDTO(model.getIdCd(), model.getIdEmpresaMunicipio());

        when(cdEmpresaMunipioMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(cdEmpresaMunipioMapper.toDTO(model)).thenReturn(response);

        when(cdEmpresaMunipioRepository.save(model)).thenReturn(Mono.just(model));
        when(cdEmpresaMunipioRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(cdEmpresaMunipioRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(cdEmpresaMunipioRepository.delete(model)).thenReturn(Mono.empty());
        when(cdEmpresaMunipioRepository.findByIdEmpresaMunicipio(model.getIdEmpresaMunicipio())).thenReturn(Flux.just(model));

        StepVerifier.create(manutencaoCdEmpresaMunicipioService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(manutencaoCdEmpresaMunicipioService.listCdEmpresaMunicipio()).expectNext(response).verifyComplete();
        StepVerifier.create(manutencaoCdEmpresaMunicipioService.delete(model.getId())).verifyComplete();
    }
}
