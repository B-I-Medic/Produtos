package com.medic.ETL.service.demanda;

import com.medic.ETL.repository.demanda.ConsultaDemandaRepository;
import com.medic.ETL.repository.demanda.InsercaoDemandaProdutoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessarDemandaServiceTest {

    private final ConsultaDemandaRepository consultaDemandaRepository = mock(ConsultaDemandaRepository.class);
    private final PrepararConsultaDemandaService prepararConsultaDemandaService = mock(PrepararConsultaDemandaService.class);
    private final InsercaoDemandaProdutoRepository insercaoDemandaProdutoRepository = mock(InsercaoDemandaProdutoRepository.class);
    private final ProcessarDemandaService service = new ProcessarDemandaService(
            consultaDemandaRepository,
            prepararConsultaDemandaService,
            insercaoDemandaProdutoRepository
    );

    @Test
    void shouldQueryAndPersistDemandRows() {
        var processamento = TestDataFactory.processamento();
        var demanda = TestDataFactory.demanda();
        when(prepararConsultaDemandaService.montarConsulta(processamento)).thenReturn("sql-demanda");
        when(consultaDemandaRepository.consultarUFX("sql-demanda")).thenReturn(List.of(demanda));

        service.atualizarDemanda(processamento);

        verify(insercaoDemandaProdutoRepository).inserirEmLote(List.of(demanda));
    }

    @Test
    void shouldNotQueryOrPersistWhenQueryIsBlank() {
        var processamento = TestDataFactory.processamento();
        when(prepararConsultaDemandaService.montarConsulta(processamento)).thenReturn(" ");

        service.atualizarDemanda(processamento);

        verify(consultaDemandaRepository, never()).consultarUFX(org.mockito.ArgumentMatchers.anyString());
        verify(insercaoDemandaProdutoRepository, never()).inserirEmLote(org.mockito.ArgumentMatchers.anyList());
    }
}
