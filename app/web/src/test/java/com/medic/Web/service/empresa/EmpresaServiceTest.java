package com.medic.Web.service.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.exception.type.NotFoundException;
import com.medic.Web.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Web.mapper.empresa.EmpresaMapper;
import com.medic.Web.mapper.empresa.EmpresaMunicipioMapper;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Web.repository.cd.MunicipioRepository;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private MunicipioRepository municipioRepository;
    @Mock
    private EmpresaMunicipioMapper empresaMunicipioMapper;
    @Mock
    private CdEmpresaMunipioMapper cdEmpresaMunipioMapper;

    @InjectMocks
    private ManutencaoEmpresaService empresaService;
    @InjectMocks
    private ManutencaoEmpresaMunicipioService empresaMunicipioService;

    @Test
    void shouldSaveEmpresa() {

        var model = TestDataFactory.empresaModel();
        var response = TestDataFactory.empresaResponseDTO();
        var municipio = TestDataFactory.municipioModel();
        var dto = new EmpresaRequestDTO("Empresa", municipio.getId(), Viman.UFX, "001", true, true, true);

        when(empresaMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(empresaMapper.toDTO(model, municipio)).thenReturn(response);

        when(municipioRepository.findById(municipio.getId())).thenReturn(Mono.just(municipio));
        when(empresaRepository.save(model)).thenReturn(Mono.just(model));
        StepVerifier.create(empresaService.save(dto, UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldUpdateEmpresa() {

        var model = TestDataFactory.empresaModel();
        var response = TestDataFactory.empresaResponseDTO();
        var municipio = TestDataFactory.municipioModel();
        var dto = new EmpresaRequestDTO("Empresa", municipio.getId(), Viman.UFX, "001", true, true, true);
        when(empresaRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(empresaMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(empresaRepository.save(model)).thenReturn(Mono.just(model));
        when(empresaMapper.toDTO(model, municipio)).thenReturn(response);
        when(municipioRepository.findById(municipio.getId())).thenReturn(Mono.just(municipio));

        StepVerifier.create(empresaService.update(model.getId(), dto, UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldNotSaveEmpresaWhenMunicipioDoesNotExist() {

        UUID municipioId = UUID.randomUUID();
        var dto = new EmpresaRequestDTO("Empresa", municipioId, Viman.UFX, "001", true, true, true);
        when(municipioRepository.findById(municipioId)).thenReturn(Mono.empty());

        StepVerifier.create(empresaService.save(dto, UUID.randomUUID()))
                .expectError(NotFoundException.class)
                .verify();

        verify(empresaRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void shouldListEmpresas() {

        var model = TestDataFactory.empresaModel();
        var response = TestDataFactory.empresaResponseDTO();
        when(empresaRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        var municipio = TestDataFactory.municipioModel();
        model.setMunicipioId(municipio.getId());
        when(municipioRepository.findById(municipio.getId())).thenReturn(Mono.just(municipio));
        when(empresaMapper.toDTO(model, municipio)).thenReturn(response);

        StepVerifier.create(empresaService.listEmpresas())
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldListEmpresaMunicipioByEmpresa() {

        var model = TestDataFactory.empresaModel();
        var response = TestDataFactory.empresaMunicipioResponseDTO();
        when(empresaRepository.listEmpresaMunicipioByIdEmpresa(model.getId()))
                .thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(empresaService.listEmpresaMunicipioByIDEmpresa(model.getId()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldDeleteEmpresa() {

        var model = TestDataFactory.empresaModel();
        when(empresaRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaService.delete(model.getId()))
                .verifyComplete();
    }

    @Test
    void shouldSaveEmpresaMunicipio() {

        var model = TestDataFactory.empresaMunicipioModel();
        UUID empresaId = UUID.randomUUID();
        UUID cdId = UUID.randomUUID();
        var dto = new EmpresaMunicipioRequestDTO(empresaId, cdId, model.getIdMunicipio());
        var cdModel = TestDataFactory.cdEmpresaMunipioModel();
        cdModel.setIdCd(cdId);
        cdModel.setIdEmpresaMunicipio(model.getId());
        var response = TestDataFactory.empresaMunicipioResponseDTO(model, cdId);

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
        StepVerifier.create(empresaMunicipioService.save(dto, UUID.randomUUID()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldListEmpresasMunicipio() {

        var response = TestDataFactory.empresaMunicipioResponseDTO();
        var filter = new EmpresaMunicipioFilterDTO("Empresa", "Cidade", "SP", "CD");
        when(empresaMunicipioRepository.getAllAndFilter(filter)).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(empresaMunicipioService.listEmpresasMunicipio(filter))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldDeleteEmpresaMunicipio() {

        var model = TestDataFactory.empresaMunicipioModel();
        when(empresaMunicipioRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaMunicipioService.delete(model.getId()))
                .verifyComplete();
    }
}
