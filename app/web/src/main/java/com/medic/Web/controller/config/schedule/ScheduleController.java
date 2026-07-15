package com.medic.Web.controller.config.schedule;

import com.medic.Web.dto.config.schedule.ScheduleResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.schedule.ManutencaoScheduleService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ManutencaoScheduleService manutencaoScheduleService;

    public ScheduleController(ManutencaoScheduleService manutencaoScheduleService) {
        this.manutencaoScheduleService = manutencaoScheduleService;
    }

    @PutMapping("/update/{idSchedule}")
    public Mono<ScheduleResponseDTO> update(@PathVariable @NotNull(message = "O ID do schedule é obrigatório") UUID idSchedule,
                                            @RequestParam @NotBlank(message = "O cron é obrigatório") String cron,
                                            @AuthenticationPrincipal UsuarioModel user) {

        return manutencaoScheduleService.atualizar(idSchedule, cron, user.getId());
    }

    @PutMapping("/enable/{idSchedule}")
    public Mono<ScheduleResponseDTO> enable(@PathVariable @NotNull(message = "O ID do schedule é obrigatório") UUID idSchedule,
                                            @AuthenticationPrincipal UsuarioModel user) {

        return manutencaoScheduleService.enable(idSchedule, user.getId());
    }

    @PutMapping("/disable/{idSchedule}")
    public Mono<ScheduleResponseDTO> disable(@PathVariable @NotNull(message = "O ID do schedule é obrigatório") UUID idSchedule,
                                             @AuthenticationPrincipal UsuarioModel user) {

        return manutencaoScheduleService.disable(idSchedule, user.getId());
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ScheduleResponseDTO> listSchedules() {

        return manutencaoScheduleService.listSchedules();
    }
}
