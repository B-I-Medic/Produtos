package com.medic.ETL.service.schedule;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.model.schedule.ScheduleModel;
import com.medic.ETL.repository.schedule.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsultaConfigScheduleService {

    private final ScheduleRepository repository;

    public ConsultaConfigScheduleService(ScheduleRepository repository) {
        this.repository = repository;
    }

    public Optional<String> getCron(ScheduleJob job) {

        return repository.findByJobAndAtivoTrue(job)
                .map(ScheduleModel::getCron);
    }
}
