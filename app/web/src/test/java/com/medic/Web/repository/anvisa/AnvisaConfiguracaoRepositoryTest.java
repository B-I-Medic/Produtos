package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import io.r2dbc.spi.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnvisaConfiguracaoRepositoryTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;
    @Mock
    private FetchSpec<Map<String, Object>> fetchSpec;

    @Test
    void shouldFindTheConfiguration() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        RowsFetchSpec<AnvisaConfiguracaoResponseDTO> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaConfiguracaoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaConfiguracaoResponseDTO> mapper = invocation.getArgument(0);
                    AnvisaConfiguracaoResponseDTO mapped = mapper.apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                });
        when(row.get("retry_cooldown_minutos", Integer.class)).thenReturn(7);

        StepVerifier.create(new AnvisaConfiguracaoRepository(databaseClient).find())
                .assertNext(result -> assertEquals(7, result.retryCooldownMinutos()))
                .verifyComplete();
    }

    @Test
    void shouldReturnTheUpdatedConfigurationWhenTheUpdateChangesOneRow() {

        UUID userId = UUID.randomUUID();
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        RowsFetchSpec<AnvisaConfiguracaoResponseDTO> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaConfiguracaoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, io.r2dbc.spi.RowMetadata, AnvisaConfiguracaoResponseDTO> mapper = invocation.getArgument(0);
                    AnvisaConfiguracaoResponseDTO mapped = mapper.apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                });
        when(row.get("retry_cooldown_minutos", Integer.class)).thenReturn(10);

        StepVerifier.create(new AnvisaConfiguracaoRepository(databaseClient).update(10, userId))
                .expectNext(new AnvisaConfiguracaoResponseDTO(10))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenNoConfigurationWasUpdated() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(0L));

        StepVerifier.create(new AnvisaConfiguracaoRepository(databaseClient).update(10, UUID.randomUUID()))
                .verifyComplete();
    }
}
