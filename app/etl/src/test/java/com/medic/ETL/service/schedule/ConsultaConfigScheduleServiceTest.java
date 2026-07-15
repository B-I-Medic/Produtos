package com.medic.ETL.service.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.model.schedule.ScheduleModel;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaConfigScheduleServiceTest {

    @Mock
    private ScheduleRepository repository;

    @InjectMocks
    private ConsultaConfigScheduleService service;

    @Test
    void shouldReturnCronWhenActiveScheduleExists() {

        var model = new ScheduleModel();
        model.setCron("0 0 * * * *");

        when(repository.findByJobAndAtivoTrue(ScheduleJob.ATUALIZAR_ESTOQUE))
                .thenReturn(Optional.of(model));

        assertEquals(Optional.of("0 0 * * * *"), service.getCron(ScheduleJob.ATUALIZAR_ESTOQUE));
    }

    @Test
    void shouldReturnEmptyWhenActiveScheduleDoesNotExist() {

        when(repository.findByJobAndAtivoTrue(ScheduleJob.ATUALIZAR_PRODUTOS))
                .thenReturn(Optional.empty());

        assertTrue(service.getCron(ScheduleJob.ATUALIZAR_PRODUTOS).isEmpty());
    }
}
