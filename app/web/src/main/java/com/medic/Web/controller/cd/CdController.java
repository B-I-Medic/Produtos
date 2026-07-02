package com.medic.Web.controller.cd;

import com.medic.Web.dto.cd.CdEmpresaMunicipioFilterDTO;
import com.medic.Web.dto.cd.CdEmpresaMunicipioResponseDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.cd.ManutencaoCDService;
import com.medic.Web.service.cd.ManutencaoCdEmpresaMunicipioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/centro-distribuicao")
public class CdController {

    private final ManutencaoCDService service;
    private final ManutencaoCdEmpresaMunicipioService empresaMunicipioService;

    public CdController(ManutencaoCDService service,
                        ManutencaoCdEmpresaMunicipioService empresaMunicipioService) {
        this.service = service;
        this.empresaMunicipioService = empresaMunicipioService;
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

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CentroDistribuicaoResponseDTO> listCD() {

        return service.listCDs();
    }

    @GetMapping(value = "/{cdId}/empresa-municipio/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CdEmpresaMunicipioResponseDTO> listCdEmpresaMunicipio(@PathVariable @NotNull(message = "O ID do CD é obrigatório") UUID cdId,
                                                                      @ModelAttribute CdEmpresaMunicipioFilterDTO filter) {

        return empresaMunicipioService.listCdEmpresaMunicipio(cdId, filter);
    }
}
