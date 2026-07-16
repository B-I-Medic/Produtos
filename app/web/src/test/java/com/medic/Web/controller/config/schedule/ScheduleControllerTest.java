package com.medic.Web.controller.config.schedule;

import com.medic.Web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.Web.model.config.schedule.ScheduleJob;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.schedule.ManutencaoScheduleService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduleControllerTest {

    private final ManutencaoScheduleService service = mock(ManutencaoScheduleService.class);
    private WebTestClient client;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        client = WebTestClient.bindToController(new ScheduleController(service))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldListSchedules() {

        var response = scheduleResponse();
        when(service.listSchedules()).thenReturn(Flux.fromIterable(List.of(response)));

        client.get()
                .uri("/schedule/get")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);

        verify(service).listSchedules();
    }

    @Test
    void shouldUpdateEnableAndDisableSchedule() {

        UUID scheduleId = UUID.randomUUID();
        var response = scheduleResponse();
        when(service.atualizar(scheduleId, "0 0 * * * *", user.getId())).thenReturn(Mono.just(response));
        when(service.enable(scheduleId, user.getId())).thenReturn(Mono.just(response));
        when(service.disable(scheduleId, user.getId())).thenReturn(Mono.just(response));

        client.put()
                .uri("/schedule/update/" + scheduleId + "?cron=0 0 * * * *")
                .exchange()
                .expectStatus().isOk();

        client.put()
                .uri("/schedule/enable/" + scheduleId)
                .exchange()
                .expectStatus().isOk();

        client.put()
                .uri("/schedule/disable/" + scheduleId)
                .exchange()
                .expectStatus().isOk();

        verify(service).atualizar(scheduleId, "0 0 * * * *", user.getId());
        verify(service).enable(scheduleId, user.getId());
        verify(service).disable(scheduleId, user.getId());
    }

    private static ScheduleResponseDTO scheduleResponse() {

        return new ScheduleResponseDTO(
                UUID.randomUUID(),
                ScheduleJob.ATUALIZAR_ESTOQUE,
                "0 0 * * * *",
                true,
                ZonedDateTime.now()
        );
    }
}
