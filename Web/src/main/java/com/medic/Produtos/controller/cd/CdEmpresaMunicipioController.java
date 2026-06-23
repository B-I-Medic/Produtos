package com.medic.Produtos.controller.cd;

import com.medic.Produtos.dto.cd.CdEmpresaMunipioRequestDTO;
import com.medic.Produtos.dto.cd.CdEmpresaMunipioResponseDTO;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.service.cd.ManutencaoCdEmpresaMunicipioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/centro-distribuicao/empresa-municipio")
public class CdEmpresaMunicipioController {

    private final ManutencaoCdEmpresaMunicipioService service;

    public CdEmpresaMunicipioController(ManutencaoCdEmpresaMunicipioService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CdEmpresaMunipioResponseDTO> listCdEmpresaMunicipio() {

        return service.listCdEmpresaMunicipio();
    }

    @PostMapping("/save")
    public Mono<CdEmpresaMunipioResponseDTO> save(@RequestBody @Valid Mono<CdEmpresaMunipioRequestDTO> dto,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(cdEmpresaMunicipio -> service.save(cdEmpresaMunicipio, user.getId()));
    }

    @DeleteMapping("/delete/{cdEmpresaMunicipioId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID do vinculo e obrigatorio") UUID cdEmpresaMunicipioId) {

        return service.delete(cdEmpresaMunicipioId);
    }
}
