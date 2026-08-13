package com.medic.ETL.service.estoque.segregado;

import com.medic.ETL.dto.consulta.EstoqueSegregadoConsultaDTO;
import com.medic.ETL.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.ETL.repository.estoque.segregado.InsercaoConsultaSegregadoProdutoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarEstoqueSegregadoServiceTest {

    private final PrepararConsultaEstoqueSegregadoService prepararConsultaService = mock(PrepararConsultaEstoqueSegregadoService.class);
    private final ConsultaConsultaSegregadoUFXRepository consultaRepository = mock(ConsultaConsultaSegregadoUFXRepository.class);
    private final InsercaoConsultaSegregadoProdutoRepository insercaoRepository = mock(InsercaoConsultaSegregadoProdutoRepository.class);
    private final ProcessarEstoqueSegregadoService service = new ProcessarEstoqueSegregadoService(
            prepararConsultaService,
            consultaRepository,
            insercaoRepository
    );

    @Test
    void shouldQueryAndPersistRows() {
        var processamento = TestDataFactory.processamento();
        var item = TestDataFactory.estoqueSegregado(10);
        when(prepararConsultaService.montarConsultas(processamento)).thenReturn(new EstoqueSegregadoConsultaDTO("sql"));
        when(consultaRepository.consultar("sql")).thenReturn(List.of(item));

        service.processarEstoqueSegregado(processamento);

        verify(insercaoRepository).inserirEmLote(List.of(item));
    }

    @Test
    void shouldNotQueryOrPersistWhenQueryIsBlank() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaService.montarConsultas(processamento)).thenReturn(new EstoqueSegregadoConsultaDTO(" "));

        service.processarEstoqueSegregado(processamento);

        verify(consultaRepository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoRepository, never()).inserirEmLote(org.mockito.ArgumentMatchers.anyList());
    }
}
