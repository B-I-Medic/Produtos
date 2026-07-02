package com.medic.Web.service.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Web.mapper.empresa.EmpresaMapper;
import com.medic.Web.mapper.empresa.EmpresaMunicipioMapper;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Web.repository.empresa.EmpresaMunicipioRepository;
import com.medic.Web.repository.empresa.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private EmpresaMapper empresaMapper;
    @Mock
    private EmpresaMunicipioRepository empresaMunicipioRepository;
    @Mock
    private CdEmpresaMunipioRepository cdEmpresaMunipioRepository;
    @Mock
    private EmpresaMunicipioMapper empresaMunicipioMapper;
    @Mock
    private CdEmpresaMunipioMapper cdEmpresaMunipioMapper;

    @InjectMocks
    private ManutencaoEmpresaService empresaService;
    @InjectMocks
    private ManutencaoEmpresaMunicipioService empresaMunicipioService;

    @Test
    void shouldCrudEmpresa() {

        var model = TestDataFactory.empresaModel();
        var response = TestDataFactory.empresaResponseDTO();
        var dto = new EmpresaRequestDTO("Empresa", Viman.UFX, "001", true, true, true);

        when(empresaMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(empresaMapper.toDTO(model)).thenReturn(response);

        when(empresaRepository.save(model)).thenReturn(Mono.just(model));
        when(empresaRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(empresaRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(empresaRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(empresaService.update(model.getId(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(empresaService.listEmpresas()).expectNext(response).verifyComplete();
        StepVerifier.create(empresaService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldCrudEmpresaMunicipio() {

        var model = TestDataFactory.empresaMunicipioModel();
        UUID empresaId = UUID.randomUUID();
        UUID cdId = UUID.randomUUID();
        var dto = new EmpresaMunicipioRequestDTO(cdId, model.getIdMunicipio());
        var cdModel = TestDataFactory.cdEmpresaMunipioModel();
        cdModel.setIdCd(cdId);
        cdModel.setIdEmpresaMunicipio(model.getId());
        var response = TestDataFactory.empresaMunicipioResponseDTO(model, cdId);
        var filter = new EmpresaMunicipioFilterDTO("Cidade", "SP");

        when(empresaMunicipioMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(empresaId),
                ArgumentMatchers.eq(model.getIdMunicipio()),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(cdEmpresaMunipioMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(cdId),
                ArgumentMatchers.eq(model.getId()),
                ArgumentMatchers.any(UUID.class))).thenReturn(cdModel);

        when(empresaMunicipioRepository.save(model)).thenReturn(Mono.just(model));
        when(cdEmpresaMunipioRepository.save(cdModel)).thenReturn(Mono.just(cdModel));
        when(empresaMunicipioRepository.findByIdCustom(model.getId())).thenReturn(Mono.just(response));
        when(empresaMunicipioRepository.findByFiltro(empresaId, filter)).thenReturn(Flux.fromIterable(List.of(response)));
        when(empresaMunicipioRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaMunicipioService.save(empresaId, dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(empresaMunicipioService.listEmpresasMunicipio(empresaId, filter)).expectNext(response).verifyComplete();
        StepVerifier.create(empresaMunicipioService.delete(model.getId())).verifyComplete();
    }
}
