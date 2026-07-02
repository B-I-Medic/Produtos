package com.medic.Web.controller.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
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

    @PostMapping("/save/{empresaId}")
    public Mono<EmpresaMunicipioResponseDTO> save(@PathVariable @NotNull(message = "O ID da Empresa é obrigatório") UUID empresaId,
                                                  @RequestBody @Valid Mono<EmpresaMunicipioRequestDTO> dto,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresaMunicipio ->
                service.save(empresaId, empresaMunicipio, user.getId()));
    }

    @DeleteMapping("/delete/{empresaMunicipioId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID da empresa-municipio e obrigatorio") UUID empresaMunicipioId) {

        return service.delete(empresaMunicipioId);
    }

    @GetMapping(value = "/get/{empresaId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmpresaMunicipioResponseDTO> listEmpresasMunicipio(@PathVariable @NotNull(message = "O ID da empresa é obrigatório") UUID empresaId,
                                                                   @ModelAttribute EmpresaMunicipioFilterDTO filter) {

        return service.listEmpresasMunicipio(empresaId, filter);
    }
}
