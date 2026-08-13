package com.medic.ETL.service.estoque.interno;

import com.medic.ETL.dto.consulta.EstoqueInternoConsultaDTO;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoS00Repository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoUFXRepository;
import com.medic.ETL.repository.estoque.interno.InsercaoEstoqueInternoProdutoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarEstoqueInternoServiceTest {

    private final PrepararConsultaEstoqueInternoService prepararConsultaEstoqueInternoService = mock(PrepararConsultaEstoqueInternoService.class);
    private final ConsultaEstoqueInternoUFXRepository consultaUfxRepository = mock(ConsultaEstoqueInternoUFXRepository.class);
    private final ConsultaEstoqueInternoS00Repository consultaS00Repository = mock(ConsultaEstoqueInternoS00Repository.class);
    private final InsercaoEstoqueInternoProdutoRepository insercaoRepository = mock(InsercaoEstoqueInternoProdutoRepository.class);
    private final Executor sameThreadExecutor = Runnable::run;
    private final ProcessarEstoqueInternoService service = new ProcessarEstoqueInternoService(
            prepararConsultaEstoqueInternoService,
            consultaUfxRepository,
            consultaS00Repository,
            insercaoRepository,
            sameThreadExecutor
    );

    @Test
    void shouldMergeUfxAndS00ResultsAndPersist() {
        var processamento = TestDataFactory.processamento();
        var ufx = TestDataFactory.estoqueInterno(10);
        var s00 = TestDataFactory.estoqueInterno(20);
        when(prepararConsultaEstoqueInternoService.montarConsultas(processamento))
                .thenReturn(new EstoqueInternoConsultaDTO("sql-ufx", "sql-s00"));
        when(consultaUfxRepository.consultar("sql-ufx")).thenReturn(List.of(ufx));
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of(s00));

        service.processarEstoqueInterno(processamento);

        verify(insercaoRepository).inserirEmLote(List.of(ufx, s00));
    }

    @Test
    void shouldNotQueryOrPersistWhenQueriesAreBlank() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaEstoqueInternoService.montarConsultas(processamento))
                .thenReturn(new EstoqueInternoConsultaDTO(null, " "));

        service.processarEstoqueInterno(processamento);

        verify(consultaUfxRepository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(consultaS00Repository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoRepository, never()).inserirEmLote(org.mockito.ArgumentMatchers.anyList());
    }
}
