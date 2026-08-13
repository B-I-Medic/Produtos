package com.medic.Web.controller.usuario;

import com.medic.Web.dto.pagina.PaginaResponseDTO;
import com.medic.Web.dto.usuario.UsuarioPaginaDTO;
import com.medic.Web.dto.usuario.UsuarioRequestDTO;
import com.medic.Web.model.usuario.Role;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.usuario.ConsultaUsuarioService;
import com.medic.Web.service.usuario.ManutencaoUsuarioService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    private final ManutencaoUsuarioService manutencaoService = mock(ManutencaoUsuarioService.class);
    private final ConsultaUsuarioService consultaService = mock(ConsultaUsuarioService.class);
    private WebTestClient client;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        client = WebTestClient.bindToController(new UsuarioController(manutencaoService, consultaService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldGetPage() {

        var response = new PaginaResponseDTO<>(List.of(TestDataFactory.usuarioResponseDTO()), 1, 1);
        when(consultaService.getPage(any(UsuarioPaginaDTO.class))).thenReturn(Mono.just(response));

        client.get()
                .uri("/usuario/get/paginado?numeroPagina=1&tamanhoPagina=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.quantidadeRegistros").isEqualTo(1);
    }

    @Test
    void shouldSaveUsuario() {

        var response = TestDataFactory.usuarioResponseDTO();
        var dto = new UsuarioRequestDTO("teste@medic.com", "Teste", Role.ADMIN);
        when(manutencaoService.save(dto, user.getId())).thenReturn(Mono.just(response));

        client.post()
                .uri("/usuario/save")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(manutencaoService).save(dto, user.getId());
    }

    @Test
    void shouldUpdateUsuario() {

        var response = TestDataFactory.usuarioResponseDTO();
        var dto = new UsuarioRequestDTO("teste@medic.com", "Teste", Role.ADMIN);
        when(manutencaoService.update(user.getId(), dto, user.getId())).thenReturn(Mono.just(response));

        client.put()
                .uri("/usuario/update/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        verify(manutencaoService).update(user.getId(), dto, user.getId());
    }

    @Test
    void shouldDisableUsuario() {

        var response = TestDataFactory.usuarioResponseDTO();
        when(manutencaoService.disable(user.getId(), user.getId())).thenReturn(Mono.just(response));

        client.put()
                .uri("/usuario/disable/" + user.getId())
                .exchange()
                .expectStatus().isOk();

        verify(manutencaoService).disable(user.getId(), user.getId());
    }

    @Test
    void shouldEnableUsuario() {

        var response = TestDataFactory.usuarioResponseDTO();
        when(manutencaoService.enable(user.getId(), user.getId())).thenReturn(Mono.just(response));

        client.put()
                .uri("/usuario/enable/" + user.getId())
                .exchange()
                .expectStatus().isOk();

        verify(manutencaoService).enable(user.getId(), user.getId());
    }
}
