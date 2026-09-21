package com.medic.ETL.service.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrarExecucaoScheduleServiceTest {

    @Mock
    private ScheduleRepository repository;

    @Test
    void shouldRegisterCurrentExecutionInstantForJob() {

        var service = new RegistrarExecucaoScheduleService(repository);
        service.registrarInicio(ScheduleJob.ATUALIZAR_ESTOQUE);

        var jobCaptor = ArgumentCaptor.forClass(ScheduleJob.class);
        var instantCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(repository).atualizarUltimaExecucao(jobCaptor.capture(), instantCaptor.capture());

        assertEquals(ScheduleJob.ATUALIZAR_ESTOQUE, jobCaptor.getValue());
        assertNotNull(instantCaptor.getValue());
    }
}
