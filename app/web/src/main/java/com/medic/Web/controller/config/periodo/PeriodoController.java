package com.medic.Web.controller.config.periodo;

import com.medic.Web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.periodo.ManutencaoPeriodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/periodo")
public class PeriodoController {

    private final ManutencaoPeriodoService service;

    public PeriodoController(ManutencaoPeriodoService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PeriodoResponseDTO> listPeriods() {

        return service.listPeriods();
    }

    @PutMapping("/definir/{periodoId}")
    public Mono<PeriodoResponseDTO> setPeriod(@RequestBody @Valid Mono<PeriodoRequestDTO> dto,
                                              @PathVariable @NotNull(message = "O ID da Periodo é obrigatório") UUID periodoId,
                                              @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(periodo ->
                service.definirPeriodo(periodoId, periodo, user.getId())
        );
    }
}
