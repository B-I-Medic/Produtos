package com.medic.etl.service.estoque.segregado;

import com.medic.etl.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.etl.repository.estoque.segregado.ConsultaConsultaSegregadoS00Repository;
import com.medic.etl.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.etl.repository.estoque.segregado.InsercaoConsultaSegregadoProdutoRepository;
import com.medic.etl.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarEstoqueSegregadoServiceTest {

    private final PrepararConsultaEstoqueSegregadoService prepararConsultaService = mock(PrepararConsultaEstoqueSegregadoService.class);
    private final ConsultaConsultaSegregadoUFXRepository consultaUfxRepository = mock(ConsultaConsultaSegregadoUFXRepository.class);
    private final ConsultaConsultaSegregadoS00Repository consultaS00Repository = mock(ConsultaConsultaSegregadoS00Repository.class);
    private final InsercaoConsultaSegregadoProdutoRepository insercaoRepository = mock(InsercaoConsultaSegregadoProdutoRepository.class);
    private final ProcessarEstoqueSegregadoService service = new ProcessarEstoqueSegregadoService(
            prepararConsultaService,
            consultaUfxRepository,
            consultaS00Repository,
            insercaoRepository
    );

    @Test
    void shouldMergeUfxAndS00ResultsAndPersist() {
        var processamento = TestDataFactory.processamento();
        var ufx = TestDataFactory.estoqueSegregado(10);
        var s00 = TestDataFactory.estoqueSegregado(20);
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new EstoqueSegregadoConsultaDTO("sql-ufx", "sql-s00"));
        when(consultaUfxRepository.consultar("sql-ufx")).thenReturn(List.of(ufx));
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of(s00));

        service.processarEstoqueSegregado(processamento);

        verify(prepararConsultaService).montarConsultas(processamento);
        verify(consultaUfxRepository).consultar("sql-ufx");
        verify(consultaS00Repository).consultar("sql-s00");
        verify(insercaoRepository).inserirEmLote(List.of(ufx, s00));
    }

    @Test
    void shouldPersistS00ResultsWhenUfxQueryIsBlank() {
        var processamento = TestDataFactory.processamento();
        var s00 = TestDataFactory.estoqueSegregado(20);
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new EstoqueSegregadoConsultaDTO(" ", "sql-s00"));
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of(s00));

        service.processarEstoqueSegregado(processamento);

        verify(consultaUfxRepository, never()).consultar(anyString());
        verify(consultaS00Repository).consultar("sql-s00");
        verify(insercaoRepository).inserirEmLote(List.of(s00));
    }

    @Test
    void shouldPersistUfxResultsWhenS00QueryIsNull() {
        var processamento = TestDataFactory.processamento();
        var ufx = TestDataFactory.estoqueSegregado(10);
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new EstoqueSegregadoConsultaDTO("sql-ufx", null));
        when(consultaUfxRepository.consultar("sql-ufx")).thenReturn(List.of(ufx));

        service.processarEstoqueSegregado(processamento);

        verify(consultaUfxRepository).consultar("sql-ufx");
        verify(consultaS00Repository, never()).consultar(anyString());
        verify(insercaoRepository).inserirEmLote(List.of(ufx));
    }

    @Test
    void shouldNotQueryOrPersistWhenBothQueriesAreBlank() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new EstoqueSegregadoConsultaDTO(null, " "));

        service.processarEstoqueSegregado(processamento);

        verify(consultaUfxRepository, never()).consultar(anyString());
        verify(consultaS00Repository, never()).consultar(anyString());
        verify(insercaoRepository, never()).inserirEmLote(anyList());
    }

    @Test
    void shouldNotPersistWhenQueriesReturnNoRows() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new EstoqueSegregadoConsultaDTO("sql-ufx", "sql-s00"));
        when(consultaUfxRepository.consultar("sql-ufx")).thenReturn(List.of());
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of());

        service.processarEstoqueSegregado(processamento);

        verify(consultaUfxRepository).consultar("sql-ufx");
        verify(consultaS00Repository).consultar("sql-s00");
        verify(insercaoRepository, never()).inserirEmLote(anyList());
    }
}
