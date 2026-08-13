package com.medic.Web.service.estoque;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteRequestDTO;
import com.medic.Web.mapper.config.estoque.EstoqueInternoMapper;
import com.medic.Web.mapper.config.estoque.EstoqueSegregadoMapper;
import com.medic.Web.mapper.config.estoque.ValePermanenteMapper;
import com.medic.Web.repository.config.estoque.interno.EstoqueInternoRepository;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.repository.config.estoque.vp.ValePermanenteRepository;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueInternoService;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.Web.service.config.estoque.ManutencaoValePermanenteService;
import com.medic.Web.support.TestDataFactory;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueInternoRepository estoqueInternoRepository;
    @Mock
    private EstoqueInternoMapper estoqueInternoMapper;
    @Mock
    private EstoqueSegregadoRepository estoqueSegregadoRepository;
    @Mock
    private EstoqueSegregadoMapper estoqueSegregadoMapper;
    @Mock
    private ValePermanenteRepository valePermanenteRepository;
    @Mock
    private ValePermanenteMapper valePermanenteMapper;
    @Mock
    private ValidadorEmpresaEstoqueService validador;

    @InjectMocks
    private ManutencaoEstoqueInternoService estoqueInternoService;
    @InjectMocks
    private ManutencaoEstoqueSegregadoService estoqueSegregadoService;
    @InjectMocks
    private ManutencaoValePermanenteService valePermanenteService;

    @Test
    void shouldSaveEstoqueInterno() {

        var model = TestDataFactory.estoqueInternoModel();
        var response = TestDataFactory.estoqueInternoResponseDTO();
        var dto = new EstoqueInternoRequestDTO(model.getIdEmpresa(), model.getIdEmpresaMunicipio());

        when(validador.validarEstoqueInterno(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(estoqueInternoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);

        when(estoqueInternoRepository.save(model)).thenReturn(Mono.just(model));
        StepVerifier.create(estoqueInternoService.save(dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldUpdateEstoqueInterno() {

        var model = TestDataFactory.estoqueInternoModel();
        var dto = new EstoqueInternoRequestDTO(model.getIdEmpresa(), model.getIdEmpresaMunicipio());
        when(validador.validarEstoqueInterno(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));
        when(estoqueInternoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(estoqueInternoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(estoqueInternoRepository.save(model)).thenReturn(Mono.just(model));

        StepVerifier.create(estoqueInternoService.update(model.getId(), dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldListEstoqueInterno() {

        var response = TestDataFactory.estoqueInternoResponseDTO();
        when(estoqueInternoRepository.getAllAndFilter(any())).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(estoqueInternoService.listEstoqueInterno(null)).expectNext(response).verifyComplete();
    }

    @Test
    void shouldDeleteEstoqueInterno() {

        var model = TestDataFactory.estoqueInternoModel();
        when(estoqueInternoRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(estoqueInternoService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldSaveEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), model.getIdEmpresaMunicipio());

        when(validador.validarEstoqueSegregado(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(estoqueSegregadoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);

        when(estoqueSegregadoRepository.save(model)).thenReturn(Mono.just(model));
        StepVerifier.create(estoqueSegregadoService.save(dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldUpdateEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), model.getIdEmpresaMunicipio());
        when(validador.validarEstoqueSegregado(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));
        when(estoqueSegregadoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(estoqueSegregadoRepository.save(model)).thenReturn(Mono.just(model));

        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldListEstoqueSegregado() {

        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        when(estoqueSegregadoRepository.getAllAndFilter(any())).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(estoqueSegregadoService.listEstoqueSegregado(null)).expectNext(response).verifyComplete();
    }

    @Test
    void shouldDeleteEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        when(estoqueSegregadoRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(estoqueSegregadoService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldSaveValePermanente() {

        var model = TestDataFactory.valePermanenteModel();
        var response = TestDataFactory.valePermanenteResponseDTO();
        var dto = new ValePermanenteRequestDTO(model.getIdEmpresa(), model.getCodVp(), model.getIdEmpresaMunicipio());

        when(validador.validarValePermanente(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(valePermanenteMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);

        when(valePermanenteRepository.save(model)).thenReturn(Mono.just(model));
        StepVerifier.create(valePermanenteService.save(dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldUpdateValePermanente() {

        var model = TestDataFactory.valePermanenteModel();
        var dto = new ValePermanenteRequestDTO(model.getIdEmpresa(), model.getCodVp(), model.getIdEmpresaMunicipio());
        when(validador.validarValePermanente(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));
        when(valePermanenteMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(valePermanenteRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(valePermanenteRepository.save(model)).thenReturn(Mono.just(model));

        StepVerifier.create(valePermanenteService.update(model.getId(), dto, UUID.randomUUID())).verifyComplete();
    }

    @Test
    void shouldListValePermanente() {

        var response = TestDataFactory.valePermanenteResponseDTO();
        when(valePermanenteRepository.getAllAndFilter(any())).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(valePermanenteService.listValePermanente(null)).expectNext(response).verifyComplete();
    }

    @Test
    void shouldDeleteValePermanente() {

        var model = TestDataFactory.valePermanenteModel();
        when(valePermanenteRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(valePermanenteService.delete(model.getId())).verifyComplete();
    }
}
