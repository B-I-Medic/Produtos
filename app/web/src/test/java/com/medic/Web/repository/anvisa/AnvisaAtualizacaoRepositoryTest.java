package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import io.r2dbc.spi.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnvisaAtualizacaoRepositoryTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;

    @Test
    void shouldMapAnUpdateWithNullAttemptsAsZero() {

        UUID id = UUID.randomUUID();
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("id", id)).thenReturn(executeSpec);
        RowsFetchSpec<AnvisaAtualizacaoResponseDTO> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaAtualizacaoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaAtualizacaoResponseDTO> mapper = invocation.getArgument(0);
                    AnvisaAtualizacaoResponseDTO mapped = mapper.apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                });
        stubRow(id, null);

        StepVerifier.create(new AnvisaAtualizacaoRepository(databaseClient).findById(id))
                .assertNext(result -> {
                    assertEquals(id, result.id());
                    assertEquals(0, result.tentativas());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapConfiguredAttemptsWhenLookingUpByDate() {

        UUID id = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 9, 18);
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("dataReferencia", date)).thenReturn(executeSpec);
        RowsFetchSpec<AnvisaAtualizacaoResponseDTO> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaAtualizacaoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaAtualizacaoResponseDTO> mapper = invocation.getArgument(0);
                    AnvisaAtualizacaoResponseDTO mapped = mapper.apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                });
        stubRow(id, 3);

        StepVerifier.create(new AnvisaAtualizacaoRepository(databaseClient).findToday(date))
                .assertNext(result -> assertEquals(3, result.tentativas()))
                .verifyComplete();
    }

    private void stubRow(UUID id, Integer tentativas) {

        lenient().when(row.get("id", UUID.class)).thenReturn(id);
        lenient().when(row.get("data_referencia", LocalDate.class)).thenReturn(LocalDate.of(2026, 9, 18));
        lenient().when(row.get("status", String.class)).thenReturn("FALHOU");
        lenient().when(row.get("tentativas", Integer.class)).thenReturn(tentativas);
        lenient().when(row.get("solicitado_em", Instant.class)).thenReturn(Instant.parse("2026-09-18T10:00:00Z"));
        lenient().when(row.get("iniciado_em", Instant.class)).thenReturn(null);
        lenient().when(row.get("concluido_em", Instant.class)).thenReturn(null);
        lenient().when(row.get("ultima_falha_em", Instant.class)).thenReturn(Instant.parse("2026-09-18T11:00:00Z"));
        lenient().when(row.get("proxima_solicitacao_em", Instant.class)).thenReturn(Instant.parse("2026-09-18T11:05:00Z"));
        lenient().when(row.get("processamento_id", UUID.class)).thenReturn(null);
        lenient().when(row.get("ultimo_erro", String.class)).thenReturn("falha");
    }
}
