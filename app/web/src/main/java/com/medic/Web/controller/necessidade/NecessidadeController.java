package com.medic.Web.controller.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoPorCDResponseDTO;
import com.medic.Web.service.necessidade.ConsultaNecessidadeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/necessidade")
public class NecessidadeController {

    private final ConsultaNecessidadeService service;

    public NecessidadeController(ConsultaNecessidadeService service) {
        this.service = service;
    }

    @GetMapping(value = "/get/agrupado-por-cd", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NecessidadeAgrupadoPorCDResponseDTO> listNecessidadesAgrupadoPorCD(@ModelAttribute NecessidadeFilterDTO filter) {

        return service.listNecessidadesAgrupadoPorCD(filter);
    }
}
