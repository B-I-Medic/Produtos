package com.medic.web.repository.forecast;

import com.medic.web.dto.forecast.AgrupamentosPadrao;
import com.medic.web.dto.forecast.ForecastFilterDTO;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastRepositoryMappingCoverageTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;

    @Test
    void shouldMapAllSelectedForecastColumns() {

        prepareQuery();
        stubRows();
        ForecastFilterDTO filter = filter(List.of(AgrupamentosPadrao.values()));

        StepVerifier.create(new ForecastRepositoryCustomImpl(databaseClient).findByFilter(filter))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("value", response.centroDistribuicao());
                    org.junit.jupiter.api.Assertions.assertEquals(1L, response.necessidadeDeCompra());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNullForColumnsOutsideTheSelectedGrouping() {

        prepareQuery();
        stubRows();
        ForecastFilterDTO filter = filter(List.of(AgrupamentosPadrao.EMPRESA));

        StepVerifier.create(new ForecastRepositoryCustomImpl(databaseClient).findByFilter(filter))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("value", response.empresa());
                    org.junit.jupiter.api.Assertions.assertNull(response.municipio());
                    org.junit.jupiter.api.Assertions.assertEquals(1L, response.qntEstoqueTotal());
                })
                .verifyComplete();
    }

    @Test
    void shouldMapProductWhenCodeProductIsGrouped() {

        prepareQuery();
        stubRows();
        ForecastFilterDTO filter = filter(List.of(AgrupamentosPadrao.COD_PRODUTO));

        StepVerifier.create(new ForecastRepositoryCustomImpl(databaseClient).findByFilter(filter))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("value", response.codigoProduto());
                    org.junit.jupiter.api.Assertions.assertEquals("value", response.produto());
                })
                .verifyComplete();
    }

    private void prepareQuery() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        lenient().when(executeSpec.bindNull(anyString(), eq(String.class))).thenReturn(executeSpec);
    }

    private void stubRows() {

        lenient().when(row.get(anyString(), eq(String.class))).thenReturn("value");
        lenient().when(row.get(anyString(), eq(Long.class))).thenReturn(1L);
        RowsFetchSpec<com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO> rows = mock(RowsFetchSpec.class);
        doAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO> mapper =
                            invocation.getArgument(0);
                    var mapped = mapper.apply(row, null);
                    lenient().when(rows.all()).thenReturn(Flux.just(mapped));
                    return rows;
                })
                .when(executeSpec)
                .map(org.mockito.ArgumentMatchers
                        .<BiFunction<Row, RowMetadata, com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO>>any());
    }

    private ForecastFilterDTO filter(List<AgrupamentosPadrao> groupBy) {
        ForecastFilterDTO filter = new ForecastFilterDTO();
        filter.setGroupBy(groupBy);
        filter.setNumeroPagina("1");
        filter.setTamanhoPagina("10");
        return filter;
    }
}
