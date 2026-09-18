package com.medic.Web.controller.anvisa;

import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoRequestDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import com.medic.Web.dto.anvisa.AnvisaEmpresaComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.anvisa.AnvisaAtualizacaoService;
import com.medic.Web.service.anvisa.ConsultaAnvisaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/anvisa")
public class AnvisaController {

    private final ConsultaAnvisaService service;
    private final AnvisaAtualizacaoService atualizacaoService;

    public AnvisaController(ConsultaAnvisaService service,
                            AnvisaAtualizacaoService atualizacaoService) {
        this.service = service;
        this.atualizacaoService = atualizacaoService;
    }

    @PostMapping("/atualizacoes")
    public Mono<ResponseEntity<AnvisaAtualizacaoResponseDTO>> solicitarAtualizacao(@AuthenticationPrincipal UsuarioModel user) {

        return atualizacaoService.solicitar(user.getId());
    }

    @GetMapping("/atualizacoes/hoje")
    public Mono<ResponseEntity<AnvisaAtualizacaoResponseDTO>> consultarAtualizacaoDeHoje() {

        return atualizacaoService.hoje()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @GetMapping(value = "/atualizacoes/{id}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AnvisaAtualizacaoResponseDTO>> acompanharAtualizacao(@PathVariable UUID id) {

        return atualizacaoService.acompanhar(id);
    }

    @GetMapping("/configuracao")
    public Mono<AnvisaConfiguracaoResponseDTO> consultarConfiguracao() {

        return atualizacaoService.configuracao();
    }

    @PutMapping("/configuracao")
    public Mono<AnvisaConfiguracaoResponseDTO> atualizarConfiguracao(@RequestBody @Valid Mono<AnvisaConfiguracaoRequestDTO> request,
                                                                     @AuthenticationPrincipal UsuarioModel user) {

        return request.flatMap(value ->
                atualizacaoService.atualizarConfiguracao(value, user.getId()));
    }

    @GetMapping(value = "/{codAnvisa}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AnvisaResponseDTO> consultar(@PathVariable String codAnvisa) {

        return service.consultar(codAnvisa);
    }

    @GetMapping(value = "/comparacao/{codAnvisa}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AnvisaEmpresaComparacaoDTO> comparar(@PathVariable String codAnvisa) {

        return service.comparar(codAnvisa);
    }
}
