package com.medic.Web.service.config.schedule;

import com.medic.Web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.Web.mapper.config.schedule.ScheduleMapper;
import com.medic.Web.repository.config.ScheduleRepository;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoScheduleService {

    private final ScheduleRepository repository;
    private final ScheduleMapper mapper;

    public ManutencaoScheduleService(ScheduleRepository repository,
                                     ScheduleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Mono<ScheduleResponseDTO> atualizar(UUID idSchedule, String cron, UUID idUser) {

        return validarCron(cron)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cron inválido")))
                .then(repository.findById(idSchedule))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Schedule não encontrado")))
                .flatMap(scheduleModel -> {

                    scheduleModel.setCron(cron);
                    scheduleModel.setAtualizadoPor(idUser);

                    return repository.save(scheduleModel);
                })
                .map(mapper::toDTO);
    }

    public Mono<ScheduleResponseDTO> enable(UUID idSchedule, UUID idUser) {

        return repository.findById(idSchedule)
                .flatMap(schedule -> {

                    schedule.setAtivo(true);
                    schedule.setAtualizadoPor(idUser);

                    return repository.save(schedule);
                })
                .map(mapper::toDTO);
    }

    public Mono<ScheduleResponseDTO> disable(UUID idSchedule, UUID idUser) {

        return repository.findById(idSchedule)
                .flatMap(schedule -> {

                    schedule.setAtivo(false);
                    schedule.setAtualizadoPor(idUser);

                    return repository.save(schedule);
                })
                .map(mapper::toDTO);
    }

    public Flux<ScheduleResponseDTO> listSchedules() {

        return repository.findAll()
                .map(mapper::toDTO);
    }

    private Mono<Boolean> validarCron(String cronExpression) {

        return Mono.justOrEmpty(cronExpression)
                .filter(cron -> !cron.isBlank())
                .filter(CronExpression::isValidExpression)
                .map(cron -> true);
    }
}
