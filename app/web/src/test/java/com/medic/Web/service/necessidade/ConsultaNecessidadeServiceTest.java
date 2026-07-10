package com.medic.Web.service.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.repository.necessidade.NecessidadeRepository;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaNecessidadeServiceTest {

    @Mock
    private NecessidadeRepository necessidadeRepository;

    @InjectMocks
    private ConsultaNecessidadeService necessidadeService;

    @Test
    void shouldListNecessidadesAgrupadas() {

        var filter = new NecessidadeFilterDTO("CD", "Empresa", "Cidade", "Produto", "Marca", null);
        var response = TestDataFactory.necessidadeAgrupadoResponseDTO();
        when(necessidadeRepository.findByFilter(filter)).thenReturn(Flux.fromIterable(List.of(response)));

        StepVerifier.create(necessidadeService.listNecessidadesAgrupadas(filter))
                .expectNext(response)
                .verifyComplete();
    }
}
