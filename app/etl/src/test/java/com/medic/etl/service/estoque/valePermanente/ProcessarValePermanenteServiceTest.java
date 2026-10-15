package com.medic.etl.service.estoque.valePermanente;

import com.medic.etl.dto.consulta.ValePermanenteConsultaDTO;
import com.medic.etl.repository.estoque.valePermanente.ConsultaS00Repository;
import com.medic.etl.repository.estoque.valePermanente.ConsultaUFXRepository;
import com.medic.etl.repository.estoque.valePermanente.InsercaoValePermanenteProdutoRepository;
import com.medic.etl.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarValePermanenteServiceTest {

    private final PrepararConsultaValePermanenteService prepararConsultaService = mock(PrepararConsultaValePermanenteService.class);
    private final ConsultaUFXRepository consultaUfxRepository = mock(ConsultaUFXRepository.class);
    private final ConsultaS00Repository consultaS00Repository = mock(ConsultaS00Repository.class);
    private final InsercaoValePermanenteProdutoRepository insercaoRepository = mock(InsercaoValePermanenteProdutoRepository.class);
    private final Executor sameThreadExecutor = Runnable::run;
    private final ProcessarValePermanenteService service = new ProcessarValePermanenteService(
            prepararConsultaService,
            consultaUfxRepository,
            consultaS00Repository,
            insercaoRepository,
            sameThreadExecutor
    );

    @Test
    void shouldMergeUfxAndS00ResultsAndPersist() {
        var processamento = TestDataFactory.processamento();
        var ufx = TestDataFactory.valePermanente(10);
        var s00 = TestDataFactory.valePermanente(20);
        ufx.setCodIBGE("3550308");
        s00.setCodIBGE("3304557");
        when(prepararConsultaService.montarConsultas(processamento)).thenReturn(new ValePermanenteConsultaDTO("sql-ufx", "sql-s00"));
        when(consultaUfxRepository.consultar("sql-ufx")).thenReturn(List.of(ufx));
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of(s00));

        service.processarValePermanente(processamento);

        verify(prepararConsultaService).montarConsultas(processamento);
        verify(consultaUfxRepository).consultar("sql-ufx");
        verify(consultaS00Repository).consultar("sql-s00");
        verify(insercaoRepository).inserirEmLote(List.of(ufx, s00));
    }

    @Test
    void shouldPersistResultsFromTheNonBlankQueryWhenTheOtherQueryIsBlank() {
        var processamento = TestDataFactory.processamento();
        var s00 = TestDataFactory.valePermanente(20);
        s00.setCodIBGE("3304557");
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new ValePermanenteConsultaDTO(" ", "sql-s00"));
        when(consultaS00Repository.consultar("sql-s00")).thenReturn(List.of(s00));

        service.processarValePermanente(processamento);

        verify(consultaUfxRepository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(consultaS00Repository).consultar("sql-s00");
        verify(insercaoRepository).inserirEmLote(List.of(s00));
    }

    @Test
    void shouldNotQueryOrPersistWhenQueriesAreBlank() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaService.montarConsultas(processamento)).thenReturn(new ValePermanenteConsultaDTO(null, " "));

        service.processarValePermanente(processamento);

        verify(consultaUfxRepository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(consultaS00Repository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoRepository, never()).inserirEmLote(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void shouldSkipNullUfxAndBlankS00Queries() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaService.montarConsultas(processamento))
                .thenReturn(new ValePermanenteConsultaDTO(null, null));

        service.processarValePermanente(processamento);

        verify(consultaUfxRepository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(consultaS00Repository, never()).consultar(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoRepository, never()).inserirEmLote(org.mockito.ArgumentMatchers.anyList());
    }
}
