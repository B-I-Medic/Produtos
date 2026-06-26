package com.medic.Produtos.service.empresa;

import com.medic.Produtos.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Produtos.dto.empresa.EmpresaRequestDTO;
import com.medic.Produtos.mapper.cd.CdEmpresaMunipioMapper;
import com.medic.Produtos.mapper.empresa.EmpresaMapper;
import com.medic.Produtos.mapper.empresa.EmpresaMunicipioMapper;
import com.medic.Produtos.model.cd.CdEmpresaMunipioModel;
import com.medic.Produtos.model.empresa.Viman;
import com.medic.Produtos.repository.cd.CdEmpresaMunipioRepository;
import com.medic.Produtos.repository.empresa.EmpresaMunicipioRepository;
import com.medic.Produtos.repository.empresa.EmpresaRepository;
import com.medic.Produtos.support.TestDataFactory;
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
        var response = TestDataFactory.empresaMunicipioResponseDTO();
        var dto = new EmpresaMunicipioRequestDTO(model.getIdEmpresa(), model.getIdMunicipio());
        CdEmpresaMunipioModel cdModel = TestDataFactory.cdEmpresaMunipioModel();

        when(empresaMunicipioMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.eq(dto),
                ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(cdEmpresaMunipioMapper.toEntity(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(CdEmpresaMunipioRequestDTO.class),
                ArgumentMatchers.any(UUID.class))).thenReturn(cdModel);
        when(empresaMunicipioMapper.toDTO(model)).thenReturn(response);

        when(empresaMunicipioRepository.save(model)).thenReturn(Mono.just(model));
        when(cdEmpresaMunipioRepository.save(cdModel)).thenReturn(Mono.just(cdModel));
        when(empresaMunicipioRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(empresaMunicipioRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaMunicipioService.save(UUID.randomUUID(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(empresaMunicipioService.listEmpresasMunicipio()).expectNext(response).verifyComplete();
        StepVerifier.create(empresaMunicipioService.delete(model.getId())).verifyComplete();
    }
}
