package com.medic.Web.controller.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoResponseDTO;
import com.medic.Web.model.necessidade.AgrupamentosPadrao;
import com.medic.Web.service.necessidade.ConsultaNecessidadeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/forecast")
public class NecessidadeController {

    private final ConsultaNecessidadeService service;

    public NecessidadeController(ConsultaNecessidadeService service) {
        this.service = service;
    }

    @GetMapping(value = {"/get"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NecessidadeAgrupadoResponseDTO> listNecessidadesAgrupadas(@ModelAttribute NecessidadeFilterDTO filter) {

        return service.listNecessidadesAgrupadas(filter);
    }

    @GetMapping(value = {"/agrupamentos-disponiveis/get"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgrupamentosPadrao> listNecessidadesAgrupadas() {

        return Flux.fromArray(AgrupamentosPadrao.values());
    }
}
