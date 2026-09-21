package com.medic.Web.controller.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.service.processamento.ConsultaProcessamentoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/processamento")
public class ProcessamentoController {

    private final ConsultaProcessamentoService service;

    public ProcessamentoController(ConsultaProcessamentoService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProcessamentoResponseDTO> consultarUltimosPorEntidade() {

        return service.consultarUltimosPorEntidade();
    }
}
