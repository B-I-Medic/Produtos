package com.medic.web.controller.estoque;

import com.medic.web.controller.config.estoque.EstoqueSegregadoCatalogoController;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoCatalogoUpdateDTO;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.web.support.FixedAuthenticationPrincipalResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

        segregadoClient = WebTestClient.bindToController(new EstoqueSegregadoCatalogoController(segregadoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldUpdateEstoqueSegregado() {

        var dto = new EstoqueSegregadoCatalogoUpdateDTO(true, TestDataFactory.municipioModel().getId());
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

}
