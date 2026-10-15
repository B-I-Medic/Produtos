package com.medic.etl.service.schedule;

import com.medic.etl.model.schedule.ScheduleJob;
import com.medic.etl.model.schedule.ScheduleModel;
import com.medic.etl.repository.schedule.ScheduleRepository;
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
