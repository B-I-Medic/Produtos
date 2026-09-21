package com.medic.ETL.service.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegistrarExecucaoScheduleService {

    private final ScheduleRepository repository;

    public RegistrarExecucaoScheduleService(ScheduleRepository repository) {
        this.repository = repository;
    }

    public void registrarInicio(ScheduleJob job) {
        repository.atualizarUltimaExecucao(job, Instant.now());
    }
}
