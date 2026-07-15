package com.medic.Web.service.config.schedule;

import com.medic.Web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.Web.mapper.config.schedule.ScheduleMapper;
import com.medic.Web.model.config.schedule.ScheduleJob;
import com.medic.Web.model.config.schedule.ScheduleModel;
import com.medic.Web.repository.config.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManutencaoScheduleServiceTest {

    @Mock
    private ScheduleRepository repository;
    @Mock
    private ScheduleMapper mapper;

    @InjectMocks
    private ManutencaoScheduleService service;

    @Test
    void shouldCrudSchedule() {

        var model = scheduleModel();
        var response = scheduleResponse();
        UUID userId = UUID.randomUUID();
        String cron = "0 0 * * * *";

        when(repository.findById(model.getId())).thenReturn(Mono.just(model));
        when(repository.save(any(ScheduleModel.class))).thenReturn(Mono.just(model));
        when(mapper.toDTO(model)).thenReturn(response);
        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));

        StepVerifier.create(service.atualizar(model.getId(), cron, userId))
                .expectNext(response)
                .verifyComplete();

        StepVerifier.create(service.enable(model.getId(), userId))
                .expectNext(response)
                .verifyComplete();

        StepVerifier.create(service.disable(model.getId(), userId))
                .expectNext(response)
                .verifyComplete();

        StepVerifier.create(service.listSchedules())
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldRejectInvalidCron() {

        when(repository.findById(any(UUID.class))).thenReturn(Mono.just(scheduleModel()));

        StepVerifier.create(service.atualizar(UUID.randomUUID(), "cron-invalido", UUID.randomUUID()))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && "Cron inválido".equals(error.getMessage()))
                .verify();
    }

    @Test
    void shouldRejectMissingSchedule() {

        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.atualizar(id, "0 0 * * * *", UUID.randomUUID()))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && "Schedule não encontrado".equals(error.getMessage()))
                .verify();
    }

    private static ScheduleModel scheduleModel() {

        var model = new ScheduleModel();
        model.setId(UUID.randomUUID());
        model.setJob(ScheduleJob.ATUALIZAR_ESTOQUE);
        model.setCron("0 0 * * * *");
        model.setAtivo(true);
        model.setAtualizadoPor(UUID.randomUUID());
        model.setAtualizadoEm(Instant.now());
        return model;
    }

    private static ScheduleResponseDTO scheduleResponse() {

        return new ScheduleResponseDTO(
                UUID.randomUUID(),
                ScheduleJob.ATUALIZAR_ESTOQUE,
                "0 0 * * * *",
                true
        );
    }
}
