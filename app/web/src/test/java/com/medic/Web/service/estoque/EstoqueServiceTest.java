package com.medic.Web.service.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoCatalogoUpdateDTO;
import com.medic.Web.repository.cd.MunicipioRepository;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.Web.support.TestDataFactory;
import com.medic.Web.validator.estoque.ValidadorEmpresaEstoqueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueSegregadoRepository estoqueSegregadoRepository;
    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private ValidadorEmpresaEstoqueService validador;

    @InjectMocks
    private ManutencaoEstoqueSegregadoService estoqueSegregadoService;

    @Test
    void shouldUpdateEstoqueSegregadoAndAssignMunicipality() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var municipio = TestDataFactory.municipioModel();
        var userId = UUID.randomUUID();
        var dto = new EstoqueSegregadoCatalogoUpdateDTO(true, municipio.getId());

        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(validador.validarEmpresa(model.getIdEmpresa())).thenReturn(Mono.empty());
        when(municipioRepository.findById(municipio.getId())).thenReturn(Mono.just(municipio));
        when(estoqueSegregadoRepository.save(model)).thenReturn(Mono.just(model));

        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, userId)).verifyComplete();

        assertTrue(model.isAtivo());
        assertEquals(municipio.getCodIbge(), model.getCodIBGE());
        assertEquals(userId, model.getAtualizadoPor());
        assertNotNull(model.getAtualizadoEm());
    }

    @Test
    void shouldRejectActivationWithoutMunicipality() {

        var model = TestDataFactory.estoqueSegregadoModel();
        model.setCodIBGE(null);
        var dto = new EstoqueSegregadoCatalogoUpdateDTO(true, null);
        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(validador.validarEmpresa(model.getIdEmpresa())).thenReturn(Mono.empty());

        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, UUID.randomUUID()))
                .expectErrorMessage("Informe o municipio para ativar o estoque segregado")
                .verify();

        verify(estoqueSegregadoRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWithoutActivationState() {
        var model = TestDataFactory.estoqueSegregadoModel();
        var dto = new EstoqueSegregadoCatalogoUpdateDTO(null, null);
        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));

        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, UUID.randomUUID()))
                .expectErrorMessage("O estado de ativacao e obrigatorio")
                .verify();

        verify(estoqueSegregadoRepository, never()).save(any());
    }

    @Test
    void shouldListEstoqueSegregado() {

        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        when(estoqueSegregadoRepository.getAllAndFilter(any())).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(estoqueSegregadoService.listEstoqueSegregado(null)).expectNext(response).verifyComplete();
    }

    @Test
    void shouldDeactivateEstoqueSegregadoAndKeepMunicipality() {

        var model = TestDataFactory.estoqueSegregadoModel();
        model.setAtivo(true);
        var municipioAntes = model.getCodIBGE();
        var dto = new EstoqueSegregadoCatalogoUpdateDTO(false, null);
        when(estoqueSegregadoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(estoqueSegregadoRepository.save(model)).thenReturn(Mono.just(model));

        StepVerifier.create(estoqueSegregadoService.update(model.getId(), dto, UUID.randomUUID())).verifyComplete();

        assertFalse(model.isAtivo());
        assertEquals(municipioAntes, model.getCodIBGE());
    }
}
