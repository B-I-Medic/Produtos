package com.medic.web.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.model.empresa.Viman;
import com.medic.web.repository.cd.CentroDistribuicaoRepositoryCustomImpl;
import com.medic.web.repository.municipio.MunicipioRepositoryCustomImpl;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepositoryCustomImpl;
import com.medic.web.repository.empresa.EmpresaRepositoryCustomImpl;
import com.medic.web.support.TestDataFactory;
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
import static org.mockito.Mockito.verify;
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

        UUID municipioId = UUID.randomUUID();
        UUID centroDistribuicaoId = UUID.randomUUID();
        prepareQuery();
        stubRowValues();
        when(row.get("id", UUID.class)).thenReturn(municipioId);
        when(row.get("descricao", String.class)).thenReturn("Cidade");
        when(row.get("cod_ibge", String.class)).thenReturn("1234567");
        when(row.get("estado", String.class)).thenReturn("SP");
        when(row.get("cd_id", UUID.class)).thenReturn(centroDistribuicaoId);
        when(row.get("cd_descricao", String.class)).thenReturn("CD Central");
        stubMappedRows();

        var filter = new MunicipioPaginaDTO();
        filter.setDescricao(" Cidade ");
        filter.setCentroDistribuicao(centroDistribuicaoId);
        StepVerifier.create(new MunicipioRepositoryCustomImpl(databaseClient)
                        .findPageByFilter(filter, 10, 0))
                .expectNextMatches(response -> response.id().equals(municipioId)
                        && response.descricao().equals("Cidade")
                        && response.codigoIbge().equals("1234567")
                        && response.estado().equals("SP")
                        && response.centroDistribuicao().id().equals(centroDistribuicaoId)
                        && response.centroDistribuicao().descricao().equals("CD Central")
                        && response.centroDistribuicao().municipio() == null)
                .verifyComplete();

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("LEFT JOIN centro_distribuicao cd"));
        verify(executeSpec).bind("descricao", "%Cidade%");
        verify(executeSpec).bind("centroDistribuicao", centroDistribuicaoId);
        assertTrue(sql.getValue().contains("cd.id = :centroDistribuicao"));

        prepareQuery();
        stubRowValues();
        when(row.get("cd_id", UUID.class)).thenReturn(null);
        stubMappedRows();
        StepVerifier.create(new MunicipioRepositoryCustomImpl(databaseClient).findPageByFilter(null, 10, 0))
                .expectNextMatches(response -> response.centroDistribuicao() == null)
                .verifyComplete();
    }

    @Test
    void shouldMapCentroDistribuicaoRowsWithMunicipioUsingSingleJoinQuery() {

        UUID centroDistribuicaoId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        prepareQuery();
        stubRowValues();
        when(row.get("cd_id", UUID.class)).thenReturn(centroDistribuicaoId);
        when(row.get("cd_descricao", String.class)).thenReturn("CD Central");
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Cidade");
        when(row.get("municipio_cod_ibge", String.class)).thenReturn("1234567");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");
        stubMappedRows();

        StepVerifier.create(new CentroDistribuicaoRepositoryCustomImpl(databaseClient).findPageByFilter(null, 10, 0))
                .expectNextMatches(response -> response.id().equals(centroDistribuicaoId)
                        && response.descricao().equals("CD Central")
                        && response.municipio() != null
                        && response.municipio().id().equals(municipioId)
                        && response.municipio().descricao().equals("Cidade")
                        && response.municipio().codigoIbge().equals("1234567")
                        && response.municipio().estado().equals("SP")
                        && response.municipio().centroDistribuicao() == null)
                .verifyComplete();

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("LEFT JOIN municipio m"));
        assertTrue(sql.getValue().contains("ON m.id = cd.municipio_id"));
    }

    @Test
    void shouldMapCentroDistribuicaoWithoutMunicipio() {

        UUID centroDistribuicaoId = UUID.randomUUID();
        prepareQuery();
        stubRowValues();
        when(row.get("cd_id", UUID.class)).thenReturn(centroDistribuicaoId);
        when(row.get("cd_descricao", String.class)).thenReturn("CD sem município");
        when(row.get("municipio_id", UUID.class)).thenReturn(null);
        stubMappedRows();

        StepVerifier.create(new CentroDistribuicaoRepositoryCustomImpl(databaseClient).findPageByFilter(null, 10, 0))
                .expectNextMatches(response -> response.id().equals(centroDistribuicaoId)
                        && response.descricao().equals("CD sem município")
                        && response.municipio() == null)
                .verifyComplete();

        verify(databaseClient).sql(anyString());
    }

    @Test
    void shouldFilterSegregatedStockByCompanyId() {

        UUID idEmpresa = UUID.randomUUID();
        prepareQuery();
        stubRowValues();
        stubMappedRows();

        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(databaseClient)
                        .findPageByEmpresaAndFilter(idEmpresa, null, 10, 0))
                .expectNextMatches(response -> response.codSegregado() == 1
                        && response.empresa().viman() == Viman.UFX)
                .verifyComplete();

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(databaseClient).sql(sql.capture());
        org.mockito.Mockito.verify(executeSpec).bind("idEmpresa", idEmpresa);
        assertTrue(sql.getValue().contains("esp.id_empresa = :idEmpresa"));
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
        lenient().when(executeSpec.bindNull(anyString(), eq(UUID.class))).thenReturn(executeSpec);
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
