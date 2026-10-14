package com.medic.Web.controller.config.estoque;

import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoCatalogoUpdateDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/estoque/segregado")
public class EstoqueSegregadoCatalogoController {

    private final ManutencaoEstoqueSegregadoService service;

    public EstoqueSegregadoCatalogoController(ManutencaoEstoqueSegregadoService service) {
        this.service = service;
    }

    @PutMapping("/update/{estoqueSegregadoId}")
    public Mono<Void> update(@RequestBody @Valid Mono<EstoqueSegregadoCatalogoUpdateDTO> dto,
                             @PathVariable @NotNull(message = "O ID do estoque segregado e obrigatorio") UUID estoqueSegregadoId,
                             @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(estoqueSegregado ->
                service.update(estoqueSegregadoId, estoqueSegregado, user.getId()));
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado(@ModelAttribute EstoqueSegregadoFilterDTO filter) {

        return service.listEstoqueSegregado(filter);
    }
}
