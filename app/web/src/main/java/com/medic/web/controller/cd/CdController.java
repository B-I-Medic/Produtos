package com.medic.web.controller.cd;

import com.medic.web.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.cd.ConsultaCDService;
import com.medic.web.service.cd.ManutencaoCDService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/centro-distribuicao")
public class CdController {

    private final ManutencaoCDService service;
    private final ConsultaCDService consultaCDService;

    public CdController(ManutencaoCDService service, ConsultaCDService consultaCDService) {
        this.service = service;
        this.consultaCDService = consultaCDService;
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

    @GetMapping("/get/paginado")
    public Mono<PaginaResponseDTO<CentroDistribuicaoResponseDTO>> getPage(@ModelAttribute @Valid CentroDistribuicaoPaginaDTO filtro) {

        return consultaCDService.getPage(filtro);
    }
}
