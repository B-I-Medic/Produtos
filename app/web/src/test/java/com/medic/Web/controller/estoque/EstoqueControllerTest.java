package com.medic.Web.controller.estoque;

import com.medic.Web.controller.config.estoque.EstoqueInternoParametroController;
import com.medic.Web.controller.config.estoque.EstoqueSegregadoParametroController;
import com.medic.Web.controller.config.estoque.ValePermanenteParametroController;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueInternoService;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.Web.service.config.estoque.ManutencaoValePermanenteService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstoqueControllerTest {

    private final ManutencaoEstoqueInternoService internoService = mock(ManutencaoEstoqueInternoService.class);
    private final ManutencaoEstoqueSegregadoService segregadoService = mock(ManutencaoEstoqueSegregadoService.class);
    private final ManutencaoValePermanenteService valeService = mock(ManutencaoValePermanenteService.class);
    private WebTestClient internoClient;
    private WebTestClient segregadoClient;
    private WebTestClient valeClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        internoClient = WebTestClient.bindToController(new EstoqueInternoParametroController(internoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
        segregadoClient = WebTestClient.bindToController(new EstoqueSegregadoParametroController(segregadoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
        valeClient = WebTestClient.bindToController(new ValePermanenteParametroController(valeService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldHandleEstoqueInternoEndpoints() {

        var model = TestDataFactory.estoqueInternoModel();
        var response = TestDataFactory.estoqueInternoResponseDTO();
        var dto = new EstoqueInternoRequestDTO(model.getIdEmpresa(), model.getIdEmpresaMunicipio());
        UUID id = UUID.randomUUID();
        when(internoService.listEstoqueInterno(any())).thenReturn(Flux.fromIterable(List.of(response)));
        when(internoService.save(dto, user.getId())).thenReturn(Mono.empty());
        when(internoService.update(id, dto, user.getId())).thenReturn(Mono.empty());
        when(internoService.delete(id)).thenReturn(Mono.empty());

        internoClient.get().uri("/estoque/interno/get").exchange().expectStatus().isOk();
        internoClient.post().uri("/estoque/interno/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        internoClient.put().uri("/estoque/interno/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        internoClient.delete().uri("/estoque/interno/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleEstoqueSegregadoEndpoints() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), model.getIdEmpresaMunicipio());
        UUID id = UUID.randomUUID();
        when(segregadoService.listEstoqueSegregado(any())).thenReturn(Flux.fromIterable(List.of(response)));
        when(segregadoService.save(dto, user.getId())).thenReturn(Mono.empty());
        when(segregadoService.update(id, dto, user.getId())).thenReturn(Mono.empty());
        when(segregadoService.delete(id)).thenReturn(Mono.empty());

        segregadoClient.get().uri("/estoque/segregado/get").exchange().expectStatus().isOk();
        segregadoClient.post().uri("/estoque/segregado/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        segregadoClient.put().uri("/estoque/segregado/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        segregadoClient.delete().uri("/estoque/segregado/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleValePermanenteEndpoints() {

        var model = TestDataFactory.valePermanenteModel();
        var response = TestDataFactory.valePermanenteResponseDTO();
        var dto = new ValePermanenteRequestDTO(model.getIdEmpresa(), model.getCodVp(), model.getIdEmpresaMunicipio());
        UUID id = UUID.randomUUID();

        when(valeService.listValePermanente(any())).thenReturn(Flux.fromIterable(List.of(response)));
        when(valeService.save(dto, user.getId())).thenReturn(Mono.empty());
        when(valeService.update(id, dto, user.getId())).thenReturn(Mono.empty());
        when(valeService.delete(id)).thenReturn(Mono.empty());

        valeClient.get().uri("/vale-permanente/get").exchange().expectStatus().isOk();
        valeClient.post().uri("/vale-permanente/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        valeClient.put().uri("/vale-permanente/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        valeClient.delete().uri("/vale-permanente/delete/" + id).exchange().expectStatus().isOk();
    }
}
