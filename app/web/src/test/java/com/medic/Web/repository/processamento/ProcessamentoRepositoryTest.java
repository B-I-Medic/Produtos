package com.medic.Web.repository.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.model.processamento.ProcessamentoDisparo;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.model.processamento.ProcessamentoStatus;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessamentoRepositoryTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private RowsFetchSpec<ProcessamentoResponseDTO> rowsFetchSpec;
    @Mock
    private Row row;

    @Test
    void shouldFindAndMapLatestSuccessfulProcessingByEntity() {

        UUID id = UUID.randomUUID();
        Instant iniciadoEm = Instant.parse("2026-09-21T17:00:00Z");
        Instant concluidoEm = Instant.parse("2026-09-21T17:05:00Z");

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, ProcessamentoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, ProcessamentoResponseDTO> mapper = invocation.getArgument(0);
                    ProcessamentoResponseDTO mapped = mapper.apply(row, null);
                    when(rowsFetchSpec.all()).thenReturn(Flux.just(mapped));
                    return rowsFetchSpec;
                });
        when(row.get("entidade", String.class)).thenReturn("ESTOQUE");
        when(row.get("id", UUID.class)).thenReturn(id);
        when(row.get("status", String.class)).thenReturn("CONCLUIDO");
        when(row.get("tipo_disparo", String.class)).thenReturn("AUTOMATICO");
        when(row.get("iniciado_em", Instant.class)).thenReturn(iniciadoEm);
        when(row.get("concluido_em", Instant.class)).thenReturn(concluidoEm);

        StepVerifier.create(new ProcessamentoRepository(databaseClient).findUltimosConcluidos())
                .assertNext(result -> {
                    assertEquals(ProcessamentoEntidade.ESTOQUE, result.entidade());
                    assertEquals(id, result.id());
                    assertEquals(ProcessamentoStatus.CONCLUIDO, result.status());
                    assertEquals(ProcessamentoDisparo.AUTOMATICO, result.tipoDisparo());
                    assertEquals(iniciadoEm, result.iniciadoEm());
                    assertEquals(concluidoEm, result.concluidoEm());
                })
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());

        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("distinct on (p.entidade)"));
        assertTrue(sql.contains("p.status = 'CONCLUIDO'"));
        assertTrue(sql.contains("p.concluido_em is not null"));
        assertTrue(sql.contains("order by p.entidade"));
        assertTrue(sql.contains("p.concluido_em desc"));
    }
}
