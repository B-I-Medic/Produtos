package com.medic.Web.controller.config.estoque;

import com.medic.Web.dto.config.estoque.vp.ValePermanenteFilterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteRequestDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.estoque.ManutencaoValePermanenteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/vale-permanente")
public class ValePermanenteParametroController {

    private final ManutencaoValePermanenteService service;

    public ValePermanenteParametroController(ManutencaoValePermanenteService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Mono<Void> save(@RequestBody @Valid Mono<ValePermanenteRequestDTO> dto,
                                                @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(valePermanente -> service.save(valePermanente, user.getId()));
    }

    @PutMapping("/update/{valePermanenteId}")
    public Mono<Void> update(@RequestBody @Valid Mono<ValePermanenteRequestDTO> dto,
                                                  @PathVariable @NotNull(message = "O ID do vale permanente e obrigatorio") UUID valePermanenteId,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(valePermanente ->
                service.update(valePermanenteId, valePermanente, user.getId()));
    }

    @DeleteMapping("/delete/{valePermanenteId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID do vale permanente e obrigatorio") UUID valePermanenteId) {

        return service.delete(valePermanenteId);
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ValePermanenteResponseDTO> listValePermanente(@ModelAttribute ValePermanenteFilterDTO filter) {

        return service.listValePermanente(filter);
    }
}
