package com.medic.web.controller.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import jakarta.validation.Valid;
import com.medic.web.service.municipio.ConsultaMunicipioService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/municipio")
public class MunicipioController {

    private final ConsultaMunicipioService service;

    public MunicipioController(ConsultaMunicipioService service) {
        this.service = service;
    }

    @GetMapping("/get/paginado")
    public Mono<PaginaResponseDTO<MunicipioResponseDTO>> getPage(@ModelAttribute @Valid MunicipioPaginaDTO filtro) {

        return service.getPage(filtro);
    }
}
