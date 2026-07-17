package com.medic.Web.controller.cd;

import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.cd.ManutencaoCDService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/centro-distribuicao")
public class CdController {

    private final ManutencaoCDService service;

    public CdController(ManutencaoCDService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Mono<CentroDistribuicaoResponseDTO> save(@RequestBody @Valid Mono<CentroDistribuicaoRequestDTO> dto,
                                                    @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(cd ->
                service.save(cd, user.getId()));
    }

    @PutMapping("/update/{cdId}")
    public Mono<CentroDistribuicaoResponseDTO> update(@PathVariable @NotNull(message = "O ID do CD e obrigatorio") UUID cdId,
                                                      @RequestBody @Valid Mono<CentroDistribuicaoRequestDTO> dto,
                                                      @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(cd ->
                service.update(cdId, cd, user.getId()));
    }

    @DeleteMapping("/delete/{cdId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID do CD e obrigatorio") UUID cdId) {

        return service.delete(cdId);
    }

    @GetMapping("/get")
    public Flux<CentroDistribuicaoResponseDTO> listCds() {

        return service.listCDs();
    }
}
