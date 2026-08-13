package com.medic.ETL.service.processamento;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.processamento.ProcessamentoRepository;
import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlarProcessamentoServiceTest {

    @Mock
    private ProcessamentoRepository repository;

    @InjectMocks
    private ControlarProcessamentoService service;

    @Test
    void shouldStartProcessingWithBusinessState() {
        ArgumentCaptor<Processamento> captor = ArgumentCaptor.forClass(Processamento.class);
        when(repository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Processamento result = service.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);

        assertSame(captor.getValue(), result);
        assertEquals(ProcessamentoStatus.INICIADO, result.getStatus());
        assertEquals(ProcessamentoEntidade.ESTOQUE, result.getEntidade());
        assertEquals(ProcessamentoDisparo.AUTOMATICO, result.getTipoDisparo());
        assertNotNull(result.getIniciadoEm());
    }

    @Test
    void shouldRegisterAbortedProcessing() {
        ArgumentCaptor<Processamento> captor = ArgumentCaptor.forClass(Processamento.class);

        service.abortarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO);

        verify(repository).save(captor.capture());
        Processamento processamento = captor.getValue();
        assertEquals(ProcessamentoStatus.ABORTADO, processamento.getStatus());
        assertEquals(ProcessamentoEntidade.DEMANDA, processamento.getEntidade());
        assertEquals(ProcessamentoDisparo.AUTOMATICO, processamento.getTipoDisparo());
        assertNotNull(processamento.getIniciadoEm());
        assertNotNull(processamento.getConcluidoEm());
    }

    @Test
    void shouldCloseProcessingWithRequestedStatus() {
        Processamento processamento = TestDataFactory.processamento();

        service.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

        assertEquals(ProcessamentoStatus.CONCLUIDO, processamento.getStatus());
        assertNotNull(processamento.getConcluidoEm());
        verify(repository).save(processamento);
    }
}
