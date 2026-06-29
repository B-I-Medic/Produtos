package com.medic.Web.controller.parametro.taxa;

import com.medic.Web.dto.parametro.taxa.TaxaRequestDTO;
import com.medic.Web.dto.parametro.taxa.TaxaResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.parametro.taxa.ManutencaoTaxaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/taxa")
public class TaxaController {

    private final ManutencaoTaxaService service;

    public TaxaController(ManutencaoTaxaService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TaxaResponseDTO> listRates() {

        return service.listRates();
    }

    @PutMapping("/definir/{taxaId}")
    public Mono<TaxaResponseDTO> setRate(@RequestBody @Valid Mono<TaxaRequestDTO> dto,
                                         @PathVariable @NotNull(message = "O ID da Taxa é obrigatório") UUID taxaId,
                                         @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(taxa ->
                service.setRate(taxaId, taxa, user.getId())
        );
    }
}
