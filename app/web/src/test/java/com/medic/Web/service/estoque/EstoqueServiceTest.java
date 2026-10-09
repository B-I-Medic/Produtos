package com.medic.Web.service.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.mapper.config.estoque.EstoqueSegregadoMapper;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
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
    private EstoqueSegregadoRepository estoqueSegregadoRepository;
    @Mock
    private EstoqueSegregadoMapper estoqueSegregadoMapper;
    @Mock
    private ValidadorEmpresaEstoqueService validador;

    @InjectMocks
    private ManutencaoEstoqueSegregadoService estoqueSegregadoService;

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

}
