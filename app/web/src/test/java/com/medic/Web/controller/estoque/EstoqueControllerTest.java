package com.medic.Web.controller.estoque;

import com.medic.Web.controller.config.estoque.EstoqueSegregadoParametroController;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.config.estoque.ManutencaoEstoqueSegregadoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstoqueControllerTest {

    private final ManutencaoEstoqueSegregadoService segregadoService = mock(ManutencaoEstoqueSegregadoService.class);
    private WebTestClient segregadoClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();

        segregadoClient = WebTestClient.bindToController(new EstoqueSegregadoParametroController(segregadoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldListEstoqueSegregado() {

        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        when(segregadoService.listEstoqueSegregado(any())).thenReturn(Flux.fromIterable(List.of(response)));

        segregadoClient.get().uri("/estoque/segregado/get").exchange().expectStatus().isOk();
        verify(segregadoService).listEstoqueSegregado(any());
    }

    @Test
    void shouldSaveEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), TestDataFactory.municipioModel().getId());
        when(segregadoService.save(dto, user.getId())).thenReturn(Mono.empty());

        segregadoClient.post()
                .uri("/estoque/segregado/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(segregadoService).save(dto, user.getId());
    }

    @Test
    void shouldUpdateEstoqueSegregado() {

        var model = TestDataFactory.estoqueSegregadoModel();
        var dto = new EstoqueSegregadoRequestDTO(model.getIdEmpresa(), model.getCodSegregado(), TestDataFactory.municipioModel().getId());
        UUID id = UUID.randomUUID();
        when(segregadoService.update(id, dto, user.getId())).thenReturn(Mono.empty());

        segregadoClient.put()
                .uri("/estoque/segregado/update/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(segregadoService).update(id, dto, user.getId());
    }

    @Test
    void shouldDeleteEstoqueSegregado() {

        UUID id = UUID.randomUUID();
        when(segregadoService.delete(id)).thenReturn(Mono.empty());

        segregadoClient.delete().uri("/estoque/segregado/delete/" + id).exchange().expectStatus().isOk();
        verify(segregadoService).delete(id);
    }

}
