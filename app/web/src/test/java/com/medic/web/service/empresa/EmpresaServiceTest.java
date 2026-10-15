package com.medic.web.service.empresa;

import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.exception.type.NotFoundException;
import com.medic.web.mapper.empresa.EmpresaMapper;
import com.medic.web.model.empresa.Viman;
import com.medic.web.repository.municipio.MunicipioRepository;
import com.medic.web.repository.empresa.EmpresaRepository;
import com.medic.web.support.TestDataFactory;
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
    private MunicipioRepository municipioRepository;

    @InjectMocks
    private ManutencaoEmpresaService empresaService;
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
    void shouldDeleteEmpresa() {

        var model = TestDataFactory.empresaModel();
        when(empresaRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(empresaService.delete(model.getId()))
                .verifyComplete();
    }

}
