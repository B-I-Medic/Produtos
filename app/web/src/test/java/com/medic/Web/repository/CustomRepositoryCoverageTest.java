package com.medic.Web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.model.empresa.Viman;
import com.medic.Web.repository.cd.MunicipioRepositoryCustomImpl;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepositoryCustomImpl;
import com.medic.Web.repository.empresa.EmpresaRepositoryCustomImpl;
import com.medic.Web.support.TestDataFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomRepositoryCoverageTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;

    @Test
    void shouldMapMunicipioRowsAndSupportNullFilters() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new MunicipioRepositoryCustomImpl(databaseClient)
                        .findByFiltro(new MunicipioFilterDTO(" Cidade ", null)))
                .expectNextCount(1)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new MunicipioRepositoryCustomImpl(databaseClient).findByFiltro(null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapSegregatedStockRowsWithAndWithoutIntegerFilter() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(new EstoqueSegregadoFilterDTO(10, "Empresa", "Cidade", "SP")))
                .expectNextMatches(response -> response.codSegregado() == 1
                        && response.empresa().viman() == Viman.UFX
                        && response.empresa().municipio() == null
                        && response.municipio() != null
                        && response.municipio().centroDistribuicao() == null)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(null))
                .expectNextCount(1)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        lenient().when(row.get("municipio_id", UUID.class)).thenReturn(null);
        stubMappedRows();
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(null))
                .expectNextMatches(response -> response.municipio() == null)
                .verifyComplete();
    }

    @Test
    void shouldOmitInverseRelationsFromNestedResponseJson() {

        var response = TestDataFactory.estoqueSegregadoResponseDTO();
        var json = new ObjectMapper().valueToTree(response);

        assertFalse(json.get("empresa").has("municipio"));
        assertFalse(json.get("municipio").has("centroDistribuicao"));
    }

    @Test
    void shouldQueryWhetherCompanyHasSegregatedStock() {
        UUID idEmpresa = UUID.randomUUID();
        prepareQuery();
        when(executeSpec.bind("idEmpresa", idEmpresa)).thenReturn(executeSpec);

        RowsFetchSpec<Boolean> rows = mock(RowsFetchSpec.class);
        doAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, RowMetadata, Boolean> mapper = invocation.getArgument(0);
                    when(row.get(0, Boolean.class)).thenReturn(true);
                    Boolean mapped = mapper.apply(row, null);
                    lenient().when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                })
                .when(executeSpec)
                .map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Boolean>>any());

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient)
                        .existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(idEmpresa))
                .expectNext(true)
                .verifyComplete();

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(databaseClient).sql(sql.capture());
        org.mockito.Mockito.verify(executeSpec).bind("idEmpresa", idEmpresa);
        assertTrue(sql.getValue().contains("possui_estoque_segregado = true"));
    }


    private void prepareQuery() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        lenient().when(executeSpec.bindNull(anyString(), eq(String.class))).thenReturn(executeSpec);
        lenient().when(executeSpec.bindNull(anyString(), eq(Integer.class))).thenReturn(executeSpec);
    }

    private void stubRowValues() {

        lenient().when(row.get(anyString(), eq(String.class))).thenReturn("value");
        lenient().when(row.get(anyString(), eq(UUID.class))).thenReturn(UUID.randomUUID());
        lenient().when(row.get(anyString(), eq(Integer.class))).thenReturn(1);
        lenient().when(row.get(anyString(), eq(Long.class))).thenReturn(1L);
        lenient().when(row.get(anyString(), eq(Boolean.class))).thenReturn(true);
        lenient().when(row.get("empresa_viman", String.class)).thenReturn("UFX");
    }

    private <T> RowsFetchSpec<T> stubMappedRows() {

        RowsFetchSpec<T> rows = mock(RowsFetchSpec.class);
        doAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    BiFunction<Row, RowMetadata, T> mapper = invocation.getArgument(0);
                    T mapped = mapper.apply(row, null);
                    lenient().when(rows.all()).thenReturn(Flux.just(mapped));
                    lenient().when(rows.one()).thenReturn(Mono.just(mapped));
                    return rows;
                })
                .when(executeSpec)
                .map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, T>>any());
        return rows;
    }
}
