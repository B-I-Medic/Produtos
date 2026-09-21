package com.medic.Web.service.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.model.processamento.ProcessamentoDisparo;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.model.processamento.ProcessamentoStatus;
import com.medic.Web.repository.processamento.ProcessamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaProcessamentoServiceTest {

    @Mock
    private ProcessamentoRepository repository;

    @Test
    void shouldReturnAllEntitiesAndFillMissingSuccessfulProcessingWithNulls() {

        var estoque = processamento(ProcessamentoEntidade.ESTOQUE);
        var demanda = processamento(ProcessamentoEntidade.DEMANDA);
        when(repository.findUltimosConcluidos()).thenReturn(Flux.just(estoque, demanda));

        StepVerifier.create(new ConsultaProcessamentoService(repository).consultarUltimosPorEntidade().collectList())
                .assertNext(results -> {
                    assertEquals(
                            List.of(
                                    ProcessamentoEntidade.ESTOQUE,
                                    ProcessamentoEntidade.PRODUTOS,
                                    ProcessamentoEntidade.DEMANDA,
                                    ProcessamentoEntidade.ANVISA
                            ),
                            results.stream().map(ProcessamentoResponseDTO::entidade).toList()
                    );
                    assertEquals(estoque, results.get(0));
                    assertNull(results.get(1).id());
                    assertNull(results.get(1).concluidoEm());
                    assertEquals(demanda, results.get(2));
                    assertNull(results.get(3).status());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnAllEntitiesWhenThereIsNoSuccessfulProcessing() {

        when(repository.findUltimosConcluidos()).thenReturn(Flux.empty());

        StepVerifier.create(new ConsultaProcessamentoService(repository).consultarUltimosPorEntidade())
                .expectNextCount(4)
                .verifyComplete();
    }

    private static ProcessamentoResponseDTO processamento(ProcessamentoEntidade entidade) {

        return new ProcessamentoResponseDTO(
                entidade,
                UUID.randomUUID(),
                ProcessamentoStatus.CONCLUIDO,
                ProcessamentoDisparo.AUTOMATICO,
                Instant.parse("2026-09-21T17:00:00Z"),
                Instant.parse("2026-09-21T17:05:00Z")
        );
    }
}
