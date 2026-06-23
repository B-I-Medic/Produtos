package com.medic.Produtos.controller.necessidade;

import com.medic.Produtos.dto.necessidade.NecessidadeResponseDTO;
import com.medic.Produtos.service.necessidade.ConsultaNecessidadeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NecessidadeResponseDTO> listNecessidades() {

        return service.listNecessidades();
    }
}
