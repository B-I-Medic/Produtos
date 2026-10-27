package com.medic.web.repository.forecast;

import com.medic.web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.web.dto.forecast.ForecastFilterDTO;
import com.medic.web.dto.forecast.AgrupamentosPadrao;
import com.medic.web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private RowsFetchSpec<ForecastAgrupadoResponseDTO> rowsFetchSpec;

    @Test
    void shouldUseDefaultGroupByWhenFilterIsNull() {

        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        stubQuery(response);

        var repository = new ForecastRepositoryCustomImpl(databaseClient);

        StepVerifier.create(repository.findByFilter(null))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("group by centro_distribuicao, empresa, estado, municipio, anvisa, marca, cod_produto"));
        assertTrue(sqlCaptor.getValue().contains("order by centro_distribuicao asc, empresa asc, necessidade_de_compra_real desc, estado asc, municipio asc, anvisa asc, marca asc, cod_produto asc"));
        verify(executeSpec).bindNull("centro_distribuicao", String.class);
        verify(executeSpec).bindNull("empresa", String.class);
        verify(executeSpec).bindNull("estado", String.class);
        verify(executeSpec).bindNull("municipio", String.class);
        verify(executeSpec).bindNull("anvisa", String.class);
        verify(executeSpec).bindNull("marca", String.class);
        verify(executeSpec).bindNull("produto", String.class);
    }

    @Test
    void shouldUseDefaultGroupByWhenFilterListIsEmpty() {
        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        stubQuery(response);
        var filter = filter(2, 20);
        filter.setGroupBy(List.of());

        StepVerifier.create(new ForecastRepositoryCustomImpl(databaseClient).findByFilter(filter))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("group by centro_distribuicao, empresa, estado, municipio, anvisa, marca, cod_produto"));
    }

    @Test
    void shouldBindFiltersAndCustomGroupBy() {

        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        stubQuery(response);

        var repository = new ForecastRepositoryCustomImpl(databaseClient);
        var filter = filter(3, 25);
        filter.setCentroDistribuicao("CD");
        filter.setEmpresa("Empresa");
        filter.setEstado("SP");
        filter.setMunicipio("Municipio");
        filter.setAnvisa("Anvisa");
        filter.setMarca("Marca");
        filter.setProduto("Produto");
        filter.setGroupBy(List.of(AgrupamentosPadrao.EMPRESA, AgrupamentosPadrao.MUNICIPIO));

        StepVerifier.create(repository.findByFilter(filter))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("select"));
        assertTrue(sqlCaptor.getValue().contains("group by empresa, municipio"));
        assertTrue(sqlCaptor.getValue().contains("cod_produto = :produto"));
        assertFalse(sqlCaptor.getValue().contains("produto ilike :produto"));
        assertFalse(sqlCaptor.getValue().contains("centro_distribuicao asc"));
        assertTrue(sqlCaptor.getValue().contains("order by empresa asc, necessidade_de_compra_real desc, municipio asc"));
        verify(executeSpec).bind("centro_distribuicao", "%CD%");
        verify(executeSpec).bind("empresa", "%Empresa%");
        verify(executeSpec).bind("estado", "%SP%");
        verify(executeSpec).bind("municipio", "%Municipio%");
        verify(executeSpec).bind("anvisa", "%Anvisa%");
        verify(executeSpec).bind("marca", "%Marca%");
        verify(executeSpec).bind("produto", "Produto");
    }

    @Test
    void shouldNotOrderByColumnsThatAreNotGrouped() {

        var response = TestDataFactory.forecastAgrupadoResponseDTO();
        stubQuery(response);

        var repository = new ForecastRepositoryCustomImpl(databaseClient);
        var filter = filter(1, 10);
        filter.setGroupBy(List.of(AgrupamentosPadrao.CENTRO_DISTRIBUICAO));

        StepVerifier.create(repository.findByFilter(filter))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("group by centro_distribuicao"));
        assertTrue(sqlCaptor.getValue().contains("order by centro_distribuicao asc, necessidade_de_compra_real desc"));
        assertFalse(sqlCaptor.getValue().contains("empresa asc"));
    }

    @Test
    void shouldUseSharedFilterPredicatesForResultAndCountQueries() {
        stubQuery(TestDataFactory.forecastAgrupadoResponseDTO());
        @SuppressWarnings("unchecked")
        RowsFetchSpec<Long> countFetchSpec = org.mockito.Mockito.mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenReturn(countFetchSpec);
        when(countFetchSpec.one()).thenReturn(reactor.core.publisher.Mono.just(1L));
        var repository = new ForecastRepositoryCustomImpl(databaseClient);
        var filter = filter(1, 10);
        filter.setGroupBy(List.of(AgrupamentosPadrao.EMPRESA));

        StepVerifier.create(repository.findByFilter(filter)).expectNextCount(1).verifyComplete();
        StepVerifier.create(repository.countByFilter(filter)).expectNext(1L).verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient, org.mockito.Mockito.times(2)).sql(sqlCaptor.capture());
        var sqls = sqlCaptor.getAllValues();
        assertTrue(sqls.get(0).contains("(cod_produto = :produto or :produto is null)"));
        assertTrue(sqls.get(1).contains("(cod_produto = :produto or :produto is null)"));
        assertFalse(sqls.get(0).contains("produto ilike :produto"));
        assertFalse(sqls.get(1).contains("produto ilike :produto"));
        verify(executeSpec, org.mockito.Mockito.times(2)).bindNull("produto", String.class);
    }

    private void stubQuery(ForecastAgrupadoResponseDTO response) {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        lenient().when(executeSpec.bindNull(anyString(), eq(String.class))).thenReturn(executeSpec);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, ForecastAgrupadoResponseDTO>>any()))
                .thenReturn(rowsFetchSpec);
        when(rowsFetchSpec.all()).thenReturn(Flux.just(response));
    }

    private ForecastFilterDTO filter(int numeroPagina, int tamanhoPagina) {
        var filter = new ForecastFilterDTO();
        filter.setNumeroPagina(String.valueOf(numeroPagina));
        filter.setTamanhoPagina(String.valueOf(tamanhoPagina));
        return filter;
    }
}
