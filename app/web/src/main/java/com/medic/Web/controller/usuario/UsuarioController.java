package com.medic.Web.controller.usuario;

import com.medic.Web.dto.pagina.PaginaResponseDTO;
import com.medic.Web.dto.usuario.UsuarioPaginaDTO;
import com.medic.Web.dto.usuario.UsuarioRequestDTO;
import com.medic.Web.dto.usuario.UsuarioResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.usuario.ConsultaUsuarioService;
import com.medic.Web.service.usuario.ManutencaoUsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final ManutencaoUsuarioService manutencaoService;
    private final ConsultaUsuarioService consultaService;

    public UsuarioController(ManutencaoUsuarioService manutencaoService,
                             ConsultaUsuarioService consultaService) {
        this.manutencaoService = manutencaoService;
        this.consultaService = consultaService;
    }

    @PostMapping("/save")
    public Mono<UsuarioResponseDTO> save(@RequestBody @Valid Mono<UsuarioRequestDTO> dto,
                                         @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(usuario ->
                manutencaoService.save(usuario, user.getId()));
    }

    @PutMapping("/update/{userId}")
    public Mono<UsuarioResponseDTO> update(@RequestBody @Valid Mono<UsuarioRequestDTO> dto,
                                           @PathVariable @NotNull(message = "O userId é obrigatório") UUID userId,
                                           @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(usuario ->
                manutencaoService.update(userId, usuario, user.getId()));
    }

    @PutMapping("/disable/{userId}")
    public Mono<UsuarioResponseDTO> disable(@PathVariable @NotNull(message = "O userId é obrigatório") UUID userId,
                                            @AuthenticationPrincipal UsuarioModel user) {

        return manutencaoService.disable(userId, user.getId());
    }

    @PutMapping("/enable/{userId}")
    public Mono<UsuarioResponseDTO> enable(@PathVariable @NotNull(message = "O userId é obrigatório") UUID userId,
                                           @AuthenticationPrincipal UsuarioModel user) {

        return manutencaoService.enable(userId, user.getId());
    }

    @GetMapping("/get/paginado")
    public Mono<PaginaResponseDTO<UsuarioResponseDTO>> getPage(@ModelAttribute @Valid UsuarioPaginaDTO dto) {

        return consultaService.getPage(dto);
    }
}
