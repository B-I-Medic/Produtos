package com.medic.Web.controller.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Web.dto.empresa.EmpresaRequestDTO;
import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.empresa.ManutencaoEmpresaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    private final ManutencaoEmpresaService service;

    public EmpresaController(ManutencaoEmpresaService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Mono<EmpresaResponseDTO> save(@RequestBody @Valid Mono<EmpresaRequestDTO> dto,
                                         @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresa -> service.save(empresa, user.getId()));
    }

    @PutMapping("/update/{empresaId}")
    public Mono<EmpresaResponseDTO> update(@RequestBody @Valid Mono<EmpresaRequestDTO> dto,
                                           @PathVariable @NotNull(message = "O ID da empresa e obrigatorio") UUID empresaId,
                                           @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresa ->
                service.update(empresaId, empresa, user.getId()));
    }

    @DeleteMapping("/delete/{empresaId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID da empresa e obrigatorio") UUID empresaId) {

        return service.delete(empresaId);
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmpresaResponseDTO> listEmpresas() {

        return service.listEmpresas();
    }

    @GetMapping(value = "/{idEmpresa}/empresa-municipio/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresaMunicipioByIDEmpresa(@PathVariable @NotNull(message = "O ID da empresa é obrigatório") UUID idEmpresa) {

        return service.listEmpresaMunicipioByIDEmpresa(idEmpresa);
    }
}
