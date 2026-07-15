package com.medic.Web.controller.estoque;

import com.medic.Web.controller.config.estoque.EstoqueInternoParametroController;
import com.medic.Web.controller.config.estoque.EstoqueSegregadoParametroController;
import com.medic.Web.controller.config.estoque.ValePermanenteParametroController;
import com.medic.Web.dto.config.estoque.EstoqueInternoRequestDTO;
import com.medic.Web.dto.config.estoque.EstoqueSegregadoRequestDTO;
import com.medic.Web.dto.config.estoque.ValePermanenteRequestDTO;
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

        var response = TestDataFactory.estoqueInternoResponseDTO();
        var dto = new EstoqueInternoRequestDTO(response.idEmpresa(), response.id_empresa_municipio());
        UUID id = UUID.randomUUID();
        when(internoService.listEstoqueInterno()).thenReturn(Flux.fromIterable(List.of(response)));
        when(internoService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(internoService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(internoService.delete(id)).thenReturn(Mono.empty());

        internoClient.get().uri("/estoque/interno/get").exchange().expectStatus().isOk();
        internoClient.post().uri("/estoque/interno/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        internoClient.put().uri("/estoque/interno/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        internoClient.delete().uri("/estoque/interno/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleEstoqueSegregadoEndpoints() {

        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        var dto = new EstoqueSegregadoRequestDTO(response.idEmpresa(), response.codSegregado(), response.id_empresa_municipio());
        UUID id = UUID.randomUUID();
        when(segregadoService.listEstoqueSegregado()).thenReturn(Flux.fromIterable(List.of(response)));
        when(segregadoService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(segregadoService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(segregadoService.delete(id)).thenReturn(Mono.empty());

        segregadoClient.get().uri("/estoque/segregado/get").exchange().expectStatus().isOk();
        segregadoClient.post().uri("/estoque/segregado/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        segregadoClient.put().uri("/estoque/segregado/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        segregadoClient.delete().uri("/estoque/segregado/delete/" + id).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleValePermanenteEndpoints() {

        var response = TestDataFactory.valePermanenteResponseDTO();
        var dto = new ValePermanenteRequestDTO(response.idEmpresa(), response.codVp(), response.id_empresa_municipio());
        UUID id = UUID.randomUUID();

        when(valeService.listValePermanente()).thenReturn(Flux.fromIterable(List.of(response)));
        when(valeService.save(dto, user.getId())).thenReturn(Mono.just(response));
        when(valeService.update(id, dto, user.getId())).thenReturn(Mono.just(response));
        when(valeService.delete(id)).thenReturn(Mono.empty());

        valeClient.get().uri("/vale-permanente/get").exchange().expectStatus().isOk();
        valeClient.post().uri("/vale-permanente/save").contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        valeClient.put().uri("/vale-permanente/update/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
        valeClient.delete().uri("/vale-permanente/delete/" + id).exchange().expectStatus().isOk();
    }
}
