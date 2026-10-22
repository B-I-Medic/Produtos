package com.medic.web.controller.config.periodo;

import com.medic.web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.periodo.ManutencaoPeriodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

    @GetMapping("/get")
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
