package com.medic.Web.service.estoque;

import com.medic.Web.dto.config.estoque.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.ValePermanenteRequestDTO;
import com.medic.Web.mapper.config.estoque.EstoqueInternoMapper;
import com.medic.Web.mapper.config.estoque.EstoqueSegregadoMapper;
import com.medic.Web.mapper.config.estoque.ValePermanenteMapper;
import com.medic.Web.repository.estoque.EstoqueInternoRepository;
import com.medic.Web.repository.estoque.EstoqueSegregadoRepository;
import com.medic.Web.repository.estoque.ValePermanenteRepository;
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
    void shouldCrudEstoqueInterno() {

        var model = TestDataFactory.estoqueInternoModel();
        var response = TestDataFactory.estoqueInternoResponseDTO();
        var dto = new EstoqueInternoRequestDTO(model.getIdEmpresa(), model.getIdEmpresaMunicipio());

        when(validador.validarEstoqueInterno(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(estoqueInternoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(estoqueInternoMapper.toDTO(model)).thenReturn(response);

        when(estoqueInternoRepository.save(model)).thenReturn(Mono.just(model));
        when(estoqueInternoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(estoqueInternoRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(estoqueInternoRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(estoqueInternoService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueInternoService.update(model.getId(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueInternoService.listEstoqueInterno()).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueInternoService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldCrudEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), model.getIdEmpresaMunicipio());

        when(validador.validarEstoqueSegregado(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(estoqueSegregadoMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(estoqueSegregadoMapper.toDTO(model)).thenReturn(response);

        when(estoqueSegregadoRepository.save(model)).thenReturn(Mono.just(model));
        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(estoqueSegregadoRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(estoqueSegregadoRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(estoqueSegregadoService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueSegregadoService.listEstoqueSegregado()).expectNext(response).verifyComplete();
        StepVerifier.create(estoqueSegregadoService.delete(model.getId())).verifyComplete();
    }

    @Test
    void shouldCrudValePermanente() {

        var model = TestDataFactory.valePermanenteModel();
        var response = TestDataFactory.valePermanenteResponseDTO();
        var dto = new ValePermanenteRequestDTO(model.getIdEmpresa(), model.getCodVp(), model.getIdEmpresaMunicipio());

        when(validador.validarValePermanente(dto.idEmpresa(), dto.id_empresa_municipio())).thenReturn(Mono.just(TestDataFactory.empresaMunicipioModel()));

        when(valePermanenteMapper.toEntity(ArgumentMatchers.any(), ArgumentMatchers.eq(dto), ArgumentMatchers.any(UUID.class))).thenReturn(model);
        when(valePermanenteMapper.toDTO(model)).thenReturn(response);

        when(valePermanenteRepository.save(model)).thenReturn(Mono.just(model));
        when(valePermanenteRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(valePermanenteRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(valePermanenteRepository.deleteById(model.getId())).thenReturn(Mono.empty());

        StepVerifier.create(valePermanenteService.save(dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(valePermanenteService.update(model.getId(), dto, UUID.randomUUID())).expectNext(response).verifyComplete();
        StepVerifier.create(valePermanenteService.listValePermanente()).expectNext(response).verifyComplete();
        StepVerifier.create(valePermanenteService.delete(model.getId())).verifyComplete();
    }
}
