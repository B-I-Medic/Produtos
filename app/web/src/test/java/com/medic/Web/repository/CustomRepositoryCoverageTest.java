package com.medic.Web.repository;

import com.medic.Web.dto.cd.CdEmpresaMunicipioFilterDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoFilterDTO;
import com.medic.Web.dto.config.estoque.segregado.EstoqueSegregadoFilterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.repository.cd.CdEmpresaMunipioRepositoryCustomImpl;
import com.medic.Web.repository.cd.MunicipioRepositoryCustomImpl;
import com.medic.Web.repository.config.estoque.interno.EstoqueInternoRepositoryCustomImpl;
import com.medic.Web.repository.config.estoque.segregado.EstoqueSegregadoRepositoryCustomImpl;
import com.medic.Web.repository.config.estoque.vp.ValePermanenteRepositoryCustomImpl;
import com.medic.Web.repository.empresa.EmpresaMunipioRepositoryCustomImpl;
import com.medic.Web.repository.empresa.EmpresaRepositoryCustomImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
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
    void shouldMapCdEmpresaMunicipioRowsAndBindOptionalFilters() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new CdEmpresaMunipioRepositoryCustomImpl(databaseClient).findByFiltro(
                        UUID.randomUUID(), new CdEmpresaMunicipioFilterDTO(" Empresa ", "", "SP")
                ))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapMunicipioRowsAndSupportNullFilters() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new MunicipioRepositoryCustomImpl(databaseClient)
                        .findByFiltro(new MunicipioFilterDTO(" Cidade ", null)))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapInternalStockRowsWithTextFilters() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new EstoqueInternoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(new EstoqueInternoFilterDTO("CD", " Empresa ", "", "SP")))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapSegregatedStockRowsWithAndWithoutIntegerFilter() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(new EstoqueSegregadoFilterDTO("CD", "Empresa", "Cidade", "SP", 10)))
                .expectNextCount(1)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapPermanentStockRowsWithAndWithoutIntegerFilter() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new ValePermanenteRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(new ValePermanenteFilterDTO("CD", "Empresa", "Cidade", "SP", 10)))
                .expectNextCount(1)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(new ValePermanenteRepositoryCustomImpl(databaseClient)
                        .getAllAndFilter(null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapEmpresaMunicipioRowsForFilterAndById() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        var repository = new EmpresaMunipioRepositoryCustomImpl(databaseClient);
        StepVerifier.create(repository.getAllAndFilter(
                        new EmpresaMunicipioFilterDTO("Empresa", "", "SP", "CD")))
                .expectNextCount(1)
                .verifyComplete();

        prepareQuery();
        stubRowValues();
        stubMappedRows();
        StepVerifier.create(repository.findByIdCustom(UUID.randomUUID()))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldMapEmpresaRowsByCompanyId() {

        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient)
                        .listEmpresaMunicipioByIdEmpresa(UUID.randomUUID()))
                .expectNextCount(1)
                .verifyComplete();
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
