package com.medic.Web.controller.municipio;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.service.municipio.ConsultaMunicipioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/municipio")
public class MunicipioController {

    private final ConsultaMunicipioService service;

    public MunicipioController(ConsultaMunicipioService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MunicipioResponseDTO> listMunicipios(@ModelAttribute MunicipioFilterDTO filter) {

        return service.listMunicipios(filter);
    }
}
