package com.medic.ETL.service.produto;

import com.medic.ETL.dto.consulta.ProdutoConsultaDTO;
import com.medic.ETL.repository.produto.ConsultaProdutoRepository;
import com.medic.ETL.repository.produto.InsercaoProdutoProdutoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarProdutoServiceTest {

    private final PrepararConsultaProdutoService prepararConsultaProdutoService = mock(PrepararConsultaProdutoService.class);
    private final ConsultaProdutoRepository consultaProdutoRepository = mock(ConsultaProdutoRepository.class);
    private final InsercaoProdutoProdutoRepository insercaoProdutoProdutoRepository = mock(InsercaoProdutoProdutoRepository.class);
    private final Executor sameThreadExecutor = Runnable::run;
    private final ProcessarProdutoService service = new ProcessarProdutoService(
            prepararConsultaProdutoService,
            consultaProdutoRepository,
            insercaoProdutoProdutoRepository,
            sameThreadExecutor
    );

    @Test
    void shouldMergeUfxAndS00ResultsAndPersist() {
        var processamento = TestDataFactory.processamento();
        var ufx = TestDataFactory.produto("UFX", "01", "P1");
        var s00 = TestDataFactory.produto("S00", "02", "P2");
        when(prepararConsultaProdutoService.montarConsultas()).thenReturn(new ProdutoConsultaDTO("sql-ufx", "sql-s00"));
        when(consultaProdutoRepository.consultarUFX("sql-ufx")).thenReturn(List.of(ufx));
        when(consultaProdutoRepository.consultarS00("sql-s00")).thenReturn(List.of(s00));

        service.atualizarProdutos(processamento);

        verify(insercaoProdutoProdutoRepository).inserirOuAtualizarEmLote(List.of(ufx, s00));
    }

    @Test
    void shouldNotQueryOrPersistWhenQueriesAreBlank() {
        when(prepararConsultaProdutoService.montarConsultas()).thenReturn(new ProdutoConsultaDTO(" ", null));

        service.atualizarProdutos(TestDataFactory.processamento());

        verify(consultaProdutoRepository, never()).consultarUFX(org.mockito.ArgumentMatchers.anyString());
        verify(consultaProdutoRepository, never()).consultarS00(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoProdutoProdutoRepository, never()).inserirOuAtualizarEmLote(org.mockito.ArgumentMatchers.anyList());
    }
}
