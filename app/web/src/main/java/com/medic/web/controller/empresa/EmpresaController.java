package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaRequestDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.pagina.PaginaResponseDTO;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.estoque.ConsultaEstoqueSegregadoService;
import com.medic.web.service.empresa.ConsultaEmpresaService;
import com.medic.web.service.empresa.ManutencaoEmpresaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    private final ManutencaoEmpresaService service;
    private final ConsultaEmpresaService consultaService;
    private final ConsultaEstoqueSegregadoService consultaEstoqueSegregadoService;

    public EmpresaController(ManutencaoEmpresaService service,
                             ConsultaEmpresaService consultaService,
                             ConsultaEstoqueSegregadoService consultaEstoqueSegregadoService) {
        this.service = service;
        this.consultaService = consultaService;
        this.consultaEstoqueSegregadoService = consultaEstoqueSegregadoService;
    }

    @PostMapping("/save")
    public Mono<EmpresaResponseDTO> save(@RequestBody @Valid Mono<EmpresaRequestDTO> dto,
                                         @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresa -> service.save(empresa, user.getId()));
    }

    @PutMapping("/update/{empresaId}")
    public Mono<EmpresaResponseDTO> update(@RequestBody @Valid Mono<EmpresaRequestDTO> dto,
                                           @PathVariable @NotNull(message = "O ID da empresa e obrigatorio") UUID empresaId,
                                           @AuthenticationPrincipal UsuarioModel user) {

        return dto.flatMap(empresa ->
                service.update(empresaId, empresa, user.getId()));
    }

    @DeleteMapping("/delete/{empresaId}")
    public Mono<Void> delete(@PathVariable @NotNull(message = "O ID da empresa e obrigatorio") UUID empresaId) {

        return service.delete(empresaId);
    }

    @GetMapping("/get/paginado")
    public Mono<PaginaResponseDTO<EmpresaResponseDTO>> getPage(@ModelAttribute @Valid EmpresaPaginaDTO dto) {

        return consultaService.getPage(dto);
    }

    @GetMapping("/{empresaId}/estoque-segregado/get/paginado")
    public Mono<PaginaResponseDTO<EstoqueSegregadoResponseDTO>> consultarEstoqueSegregado(@PathVariable @NotNull(message = "O ID da empresa e obrigatorio") UUID empresaId,
                                                                                          @ModelAttribute @Valid EstoqueSegregadoPaginaDTO filtro) {

        return consultaEstoqueSegregadoService.getPage(empresaId, filtro);
    }

}
