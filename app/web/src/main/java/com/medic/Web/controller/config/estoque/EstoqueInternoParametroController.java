package com.medic.Web.controller.config.estoque;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoFilterDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueInternoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/estoque/interno")
public class EstoqueInternoParametroController {

    private final ManutencaoEstoqueInternoService service;

    public EstoqueInternoParametroController(ManutencaoEstoqueInternoService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public Mono<Void> save(@RequestBody @Valid Mono<EstoqueInternoRequestDTO> dto,
                                                @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(estoqueInterno -> service.save(estoqueInterno, user.getId()));
    }

    @PutMapping("/update/{estoqueInternoId}")
    public Mono<Void> update(@RequestBody @Valid Mono<EstoqueInternoRequestDTO> dto,
                                                  @PathVariable @NotNull(message = "O ID do estoque interno e obrigatorio") UUID estoqueInternoId,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(estoqueInterno ->
                service.update(estoqueInternoId, estoqueInterno, user.getId()));
    }

    @DeleteMapping("/delete/{estoqueInternoId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID do estoque interno e obrigatorio") UUID estoqueInternoId) {

        return service.delete(estoqueInternoId);
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EstoqueInternoResponseDTO> listEstoqueInterno(@ModelAttribute EstoqueInternoFilterDTO filter) {

        return service.listEstoqueInterno(filter);
    }
}
