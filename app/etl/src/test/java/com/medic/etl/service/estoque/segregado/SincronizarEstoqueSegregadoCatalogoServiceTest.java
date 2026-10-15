package com.medic.etl.service.estoque.segregado;

import com.medic.etl.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import com.medic.etl.model.empresa.EmpresaModel;
import com.medic.etl.repository.empresa.EmpresaRepository;
import com.medic.etl.repository.catalogo.EstoqueSegregadoCatalogoOrigemRepository;
import com.medic.etl.repository.catalogo.EstoqueSegregadoCatalogoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SincronizarEstoqueSegregadoCatalogoServiceTest {

    private final EmpresaRepository empresas = mock(EmpresaRepository.class);
    private final EstoqueSegregadoCatalogoOrigemRepository origem = mock(EstoqueSegregadoCatalogoOrigemRepository.class);
    private final EstoqueSegregadoCatalogoRepository catalogo = mock(EstoqueSegregadoCatalogoRepository.class);
    private final SincronizarEstoqueSegregadoCatalogoService service =
            new SincronizarEstoqueSegregadoCatalogoService(empresas, origem, catalogo);

    @Test
    void shouldCollectUfxAndS00ItemsAndReconcileOneSnapshot() {
        EmpresaModel ufx = empresa(" uFx ");
        EmpresaModel s00 = empresa("s00");
        var itemUfx = new EstoqueSegregadoCatalogoItemDTO(ufx.getId(), 10, "UFX item");
        var itemS00 = new EstoqueSegregadoCatalogoItemDTO(s00.getId(), 20, "S00 item");
        when(empresas.findAllByPossuiEstoqueSegregadoIsTrue()).thenReturn(List.of(ufx, s00));
        when(origem.consultarUfx(ufx)).thenReturn(List.of(itemUfx));
        when(origem.consultarS00(s00)).thenReturn(List.of(itemS00));

        service.sincronizar();

        verify(catalogo).reconciliar(Map.of(ufx.getId(), List.of(itemUfx), s00.getId(), List.of(itemS00)));
    }

    @Test
    void shouldReconcileAnEmptySnapshotWhenThereAreNoEligibleCompanies() {
        when(empresas.findAllByPossuiEstoqueSegregadoIsTrue()).thenReturn(List.of());

        service.sincronizar();

        verify(catalogo).reconciliar(Map.of());
    }

    @Test
    void shouldRejectCompaniesWithoutSupportedSourceAndSkipReconciliation() {
        EmpresaModel empresa = empresa(null);
        when(empresas.findAllByPossuiEstoqueSegregadoIsTrue()).thenReturn(List.of(empresa));

        assertEquals("Viman sem consulta de estoque segregado: null",
                assertThrows(IllegalStateException.class, service::sincronizar).getMessage());

        verify(catalogo, never()).reconciliar(org.mockito.ArgumentMatchers.anyMap());
    }

    private EmpresaModel empresa(String viman) {
        EmpresaModel empresa = new EmpresaModel();
        empresa.setId(UUID.randomUUID());
        empresa.setViman(viman);
        return empresa;
    }
}
