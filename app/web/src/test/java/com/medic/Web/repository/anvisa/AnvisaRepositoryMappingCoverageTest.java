package com.medic.Web.repository.anvisa;

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

import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnvisaRepositoryMappingCoverageTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;

    @Test
    void shouldExecuteTheProductMapper() {

        prepareQuery();
        stubRows();

        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findProduto("123"))
                .assertNext(product -> org.junit.jupiter.api.Assertions.assertEquals("value", product.nomeComercial()))
                .verifyComplete();
    }

    @Test
    void shouldExecuteTheModelMapper() {

        prepareQuery();
        stubRows();

        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findModelos("123"))
                .assertNext(model -> org.junit.jupiter.api.Assertions.assertEquals("value", model.dsModeloProdutoMedico()))
                .verifyComplete();
    }

    @Test
    void shouldExecuteTheCompanyAndLocalProductMappers() {

        prepareQuery();
        stubRows();
        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findEmpresas())
                .assertNext(company -> org.junit.jupiter.api.Assertions.assertEquals("value", company.codigoEmpresa()))
                .verifyComplete();

        prepareQuery();
        stubRows();
        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findProdutosLocais("123"))
                .assertNext(product -> org.junit.jupiter.api.Assertions.assertEquals("value", product.codProduto()))
                .verifyComplete();
    }

    private void prepareQuery() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
    }

    private <T> RowsFetchSpec<T> stubRows() {

        RowsFetchSpec<T> rows = mock(RowsFetchSpec.class);
        lenient().when(row.get(anyString(), eq(String.class))).thenReturn("value");
        doAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, T> mapper = invocation.getArgument(0);
                    T mapped = mapper.apply(row, null);
                    lenient().when(rows.all()).thenReturn(Flux.just(mapped));
                    return rows;
                })
                .when(executeSpec)
                .map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, T>>any());
        return rows;
    }
}
