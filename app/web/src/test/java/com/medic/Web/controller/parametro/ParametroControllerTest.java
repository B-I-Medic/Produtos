package com.medic.Web.controller.parametro;

import com.medic.Web.controller.parametro.periodo.PeriodoController;
import com.medic.Web.controller.parametro.taxa.TaxaController;
import com.medic.Web.dto.parametro.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.parametro.taxa.TaxaRequestDTO;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.service.parametro.periodo.ManutencaoPeriodoService;
import com.medic.Web.service.parametro.taxa.ManutencaoTaxaService;
import com.medic.Web.support.FixedAuthenticationPrincipalResolver;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParametroControllerTest {

    private final ManutencaoPeriodoService periodoService = mock(ManutencaoPeriodoService.class);
    private final ManutencaoTaxaService taxaService = mock(ManutencaoTaxaService.class);
    private WebTestClient periodoClient;
    private WebTestClient taxaClient;
    private UsuarioModel user;

    @BeforeEach
    void setUp() {

        user = TestDataFactory.usuarioModel();
        periodoClient = WebTestClient.bindToController(new PeriodoController(periodoService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
        taxaClient = WebTestClient.bindToController(new TaxaController(taxaService))
                .argumentResolvers(configurer -> configurer.addCustomResolver(new FixedAuthenticationPrincipalResolver(user)))
                .build();
    }

    @Test
    void shouldHandlePeriodoEndpoints() {

        var response = TestDataFactory.periodoResponseDTO();
        var dto = new PeriodoRequestDTO(LocalDate.now(), LocalDate.now().plusDays(1));
        UUID id = UUID.randomUUID();
        when(periodoService.listPeriods()).thenReturn(Flux.fromIterable(List.of(response)));
        when(periodoService.definirPeriodo(id, dto, user.getId())).thenReturn(Mono.just(response));

        periodoClient.get().uri("/periodo/get").exchange().expectStatus().isOk();
        periodoClient.put().uri("/periodo/definir/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
    }

    @Test
    void shouldHandleTaxaEndpoints() {

        var response = TestDataFactory.taxaResponseDTO();
        var dto = new TaxaRequestDTO(BigDecimal.ONE);
        UUID id = UUID.randomUUID();
        when(taxaService.listRates()).thenReturn(Flux.fromIterable(List.of(response)));
        when(taxaService.setRate(id, dto, user.getId())).thenReturn(Mono.just(response));

        taxaClient.get().uri("/taxa/get").exchange().expectStatus().isOk();
        taxaClient.put().uri("/taxa/definir/" + id).contentType(MediaType.APPLICATION_JSON).bodyValue(dto).exchange().expectStatus().isOk();
    }
}
