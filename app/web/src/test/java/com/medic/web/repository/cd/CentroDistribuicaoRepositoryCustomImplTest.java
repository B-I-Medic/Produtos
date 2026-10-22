package com.medic.web.repository.cd;

import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CentroDistribuicaoRepositoryCustomImplTest {

    @Test
    void shouldFilterAndPageSortingByMunicipalityDescription() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        var metadata = mock(RowMetadata.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<CentroDistribuicaoResponseDTO> rows = mock(RowsFetchSpec.class);
        UUID cdId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("cd_id", UUID.class)).thenReturn(cdId);
        when(row.get("cd_descricao", String.class)).thenReturn("CD Central");
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Campinas");
        when(row.get("municipio_cod_ibge", String.class)).thenReturn("123");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, CentroDistribuicaoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    var mapper = invocation.<BiFunction<Row, RowMetadata, CentroDistribuicaoResponseDTO>>getArgument(0);
                    var result = mapper.apply(row, metadata);
                    when(rows.all()).thenReturn(Flux.just(result));
                    return rows;
                });
        var filtro = new CentroDistribuicaoPaginaDTO();
        filtro.setDescricao("CD Central");
        filtro.setMunicipio(municipioId);
        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC");

        StepVerifier.create(new CentroDistribuicaoRepositoryCustomImpl(client).findPageByFilter(filtro, 5, 10))
                .expectNextMatches(cd -> cd.id().equals(cdId) && cd.municipio().id().equals(municipioId)
                        && cd.municipio().descricao().equals("Campinas"))
                .verifyComplete();
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("LOWER(TRIM(cd.descricao)) = LOWER(TRIM(:descricao))"));
        assertTrue(sql.getValue().contains("cd.municipio_id = :municipio"));
        assertTrue(sql.getValue().contains("ORDER BY LOWER(m.descricao) DESC, cd.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
        verify(query).bind("descricao", "CD Central");
        verify(query).bind("municipio", municipioId);
        verify(query).bind("limite", 5L);
        verify(query).bind("deslocamento", 10L);
    }

    @Test
    void shouldRejectUnsupportedSortFieldBeforeQuery() {
        var client = mock(DatabaseClient.class);
        var filtro = new CentroDistribuicaoPaginaDTO();
        filtro.setOrdenarPor("municipio; DROP TABLE centro_distribuicao");
        assertThrows(IllegalArgumentException.class,
                () -> new CentroDistribuicaoRepositoryCustomImpl(client).findPageByFilter(filtro, 10, 0));
    }

    @Test
    void shouldCountUsingTheSameFilters() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<Long> rows = mock(RowsFetchSpec.class);
        UUID municipioId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("total", Long.class)).thenReturn(4L);
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenAnswer(invocation -> {
                    Long result = invocation.<BiFunction<Row, RowMetadata, Long>>getArgument(0).apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(result));
                    return rows;
                });
        var filtro = new CentroDistribuicaoPaginaDTO();
        filtro.setMunicipio(municipioId);
        StepVerifier.create(new CentroDistribuicaoRepositoryCustomImpl(client).countByFilter(filtro))
                .expectNext(4L).verifyComplete();
        verify(query).bind("municipio", municipioId);
    }
}
