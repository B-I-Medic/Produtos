package com.medic.Web.controller.anvisa;

import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoRequestDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.anvisa.AnvisaAtualizacaoService;
import com.medic.Web.service.anvisa.ConsultaAnvisaService;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnvisaControllerCoverageTest {

    private final ConsultaAnvisaService consultaService = mock(ConsultaAnvisaService.class);
    private final AnvisaAtualizacaoService atualizacaoService = mock(AnvisaAtualizacaoService.class);
    private final AnvisaController controller = new AnvisaController(consultaService, atualizacaoService);

    @Test
    void shouldDelegateUpdateRequestAndAuthenticatedUser() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        AnvisaAtualizacaoResponseDTO response = response("SOLICITADA");
        when(atualizacaoService.solicitar(user.getId())).thenReturn(Mono.just(ResponseEntity.accepted().body(response)));

        ResponseEntity<AnvisaAtualizacaoResponseDTO> result = controller.solicitarAtualizacao(user).block();

        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(atualizacaoService).solicitar(user.getId());
    }

    @Test
    void shouldReturnTodayOrNoContent() {

        AnvisaAtualizacaoResponseDTO response = response("CONCLUIDA");
        when(atualizacaoService.hoje()).thenReturn(Mono.just(response));
        assertEquals(HttpStatus.OK, controller.consultarAtualizacaoDeHoje().block().getStatusCode());

        when(atualizacaoService.hoje()).thenReturn(Mono.empty());
        assertEquals(HttpStatus.NO_CONTENT, controller.consultarAtualizacaoDeHoje().block().getStatusCode());
    }

    @Test
    void shouldDelegateStreamingConfigurationAndComparisonOperations() {

        UUID id = UUID.randomUUID();
        AnvisaAtualizacaoResponseDTO response = response("FALHOU");
        ServerSentEvent<AnvisaAtualizacaoResponseDTO> event = ServerSentEvent.<AnvisaAtualizacaoResponseDTO>builder()
                .event("anvisa-atualizacao")
                .id(id.toString())
                .data(response)
                .build();
        AnvisaConfiguracaoResponseDTO config = new AnvisaConfiguracaoResponseDTO(5);
        UsuarioModel user = TestDataFactory.usuarioModel();

        when(atualizacaoService.acompanhar(id)).thenReturn(Flux.just(event));
        when(atualizacaoService.configuracao()).thenReturn(Mono.just(config));
        when(atualizacaoService.atualizarConfiguracao(new AnvisaConfiguracaoRequestDTO(10), user.getId()))
                .thenReturn(Mono.just(new AnvisaConfiguracaoResponseDTO(10)));

        assertEquals(event, controller.acompanharAtualizacao(id).blockFirst());
        assertEquals(config, controller.consultarConfiguracao().block());
        assertEquals(new AnvisaConfiguracaoResponseDTO(10), controller.atualizarConfiguracao(
                Mono.just(new AnvisaConfiguracaoRequestDTO(10)), user
        ).block());
    }

    private AnvisaAtualizacaoResponseDTO response(String status) {

        return new AnvisaAtualizacaoResponseDTO(
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 18),
                status,
                1,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
