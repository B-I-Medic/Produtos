package com.medic.Produtos.controller.parametro.estoque;

import com.medic.Produtos.dto.parametro.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Produtos.dto.parametro.estoque.EstoqueSegregadoResponseDTO;
import com.medic.Produtos.model.usuario.UsuarioModel;
import com.medic.Produtos.service.parametro.estoque.ManutencaoEstoqueSegregadoService;
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
public class EstoqueSegregadoParametroController {

    private final ManutencaoEstoqueSegregadoService service;

    public EstoqueSegregadoParametroController(ManutencaoEstoqueSegregadoService service) {
        this.service = service;
    }

    @GetMapping(value = "/get", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EstoqueSegregadoResponseDTO> listEstoqueSegregado() {

        return service.listEstoqueSegregado();
    }

    @PostMapping("/save")
    public Mono<EstoqueSegregadoResponseDTO> save(@RequestBody @Valid Mono<EstoqueSegregadoRequestDTO> dto,
                                                  @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(estoqueSegregado -> service.save(estoqueSegregado, user.getId()));
    }

    @PutMapping("/update/{estoqueSegregadoId}")
    public Mono<EstoqueSegregadoResponseDTO> update(@RequestBody @Valid Mono<EstoqueSegregadoRequestDTO> dto,
                                                    @PathVariable @NotNull(message = "O ID do estoque segregado e obrigatorio") UUID estoqueSegregadoId,
                                                    @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(estoqueSegregado ->
                service.update(estoqueSegregadoId, estoqueSegregado, user.getId()));
    }

    @DeleteMapping("/delete/{estoqueSegregadoId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID do estoque segregado e obrigatorio") UUID estoqueSegregadoId) {

        return service.delete(estoqueSegregadoId);
    }
}
