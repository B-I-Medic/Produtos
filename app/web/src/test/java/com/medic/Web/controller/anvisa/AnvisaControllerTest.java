package com.medic.Web.controller.anvisa;

import com.medic.Web.dto.anvisa.AnvisaEmpresaComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoComparacaoDTO;
import com.medic.Web.dto.anvisa.AnvisaResponseDTO;
import com.medic.Web.service.anvisa.AnvisaAtualizacaoService;
import com.medic.Web.service.anvisa.ConsultaAnvisaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnvisaControllerTest {

    @Test
    void shouldExposeOfficialAnvisaDataAsSse() {

        ConsultaAnvisaService service = mock(ConsultaAnvisaService.class);
        AnvisaAtualizacaoService atualizacaoService = mock(AnvisaAtualizacaoService.class);
        var response = new AnvisaResponseDTO(
                new AnvisaProdutoDTO("81420890023", "processo", "Canulas", "II", "Produto", "cnpj", "Detentor", "Fabricante", "Brasil", "17/06/2019", "VIGENTE"),
                List.of()
        );
        when(service.consultar("81420890023")).thenReturn(Flux.just(response));

        WebTestClient.bindToController(new AnvisaController(service, atualizacaoService))
                .build()
                .get()
                .uri("/anvisa/81420890023")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AnvisaResponseDTO.class)
                .hasSize(1)
                .contains(response);
    }

    @Test
    void shouldExposeCompaniesWithProductsAsSse() {

        ConsultaAnvisaService service = mock(ConsultaAnvisaService.class);
        AnvisaAtualizacaoService atualizacaoService = mock(AnvisaAtualizacaoService.class);
        var response = new AnvisaEmpresaComparacaoDTO(
                "Alpine",
                "S00",
                List.of(new AnvisaProdutoComparacaoDTO("P1", "Produto"))
        );
        when(service.comparar("81420890023")).thenReturn(Flux.just(response));

        WebTestClient.bindToController(new AnvisaController(service, atualizacaoService))
                .build()
                .get()
                .uri("/anvisa/comparacao/81420890023")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(AnvisaEmpresaComparacaoDTO.class)
                .hasSize(1)
                .contains(response);

        verify(service).comparar("81420890023");
    }
}
