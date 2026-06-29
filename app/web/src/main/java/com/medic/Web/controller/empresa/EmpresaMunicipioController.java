package com.medic.Web.controller.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioRequestDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.empresa.ManutencaoEmpresaMunicipioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/empresa/municipio")
public class EmpresaMunicipioController {

    private final ManutencaoEmpresaMunicipioService service;

    public EmpresaMunicipioController(ManutencaoEmpresaMunicipioService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresasMunicipio() {

        return service.listEmpresasMunicipio();
    }

    @PostMapping("/save/{cdId}")
    public Mono<EmpresaMunicipioResponseDTO> save(@RequestBody @Valid Mono<EmpresaMunicipioRequestDTO> dto,
                                                  @PathVariable @NotNull(message = "O ID do CD e obrigatorio") UUID cdId,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresaMunicipio ->
                service.save(cdId, empresaMunicipio, user.getId()));
    }

    @DeleteMapping("/delete/{empresaMunicipioId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID da empresa-municipio e obrigatorio") UUID empresaMunicipioId) {

        return service.delete(empresaMunicipioId);
    }
}
