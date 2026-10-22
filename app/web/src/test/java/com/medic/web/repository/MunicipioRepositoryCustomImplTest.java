package com.medic.web.repository.municipio;

import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
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

class MunicipioRepositoryCustomImplTest {

    @Test
    void shouldFilterByDistributionCenterIdAndSortByItsDescription() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        var metadata = mock(RowMetadata.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<MunicipioResponseDTO> rows = mock(RowsFetchSpec.class);
        UUID municipioId = UUID.randomUUID();
        UUID cdId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("id", UUID.class)).thenReturn(municipioId);
        when(row.get("descricao", String.class)).thenReturn("Cidade A");
        when(row.get("cod_ibge", String.class)).thenReturn("123");
        when(row.get("estado", String.class)).thenReturn("SP");
        when(row.get("cd_id", UUID.class)).thenReturn(cdId);
        when(row.get("cd_descricao", String.class)).thenReturn("CD Central");
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, MunicipioResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    var mapper = invocation.<BiFunction<Row, RowMetadata, MunicipioResponseDTO>>getArgument(0);
                    var municipio = mapper.apply(row, metadata);
                    when(rows.all()).thenReturn(Flux.just(municipio));
                    return rows;
                });
        var filtro = new MunicipioPaginaDTO();
        filtro.setDescricao("Cidade");
        filtro.setEstado("SP");
        filtro.setCentroDistribuicao(cdId);
        filtro.setOrdenarPor("centroDistribuicao");
        filtro.setDirecaoOrdenacao("DESC");

        StepVerifier.create(new MunicipioRepositoryCustomImpl(client).findPageByFilter(filtro, 10, 20))
                .expectNextMatches(municipio -> municipio.id().equals(municipioId)
                        && municipio.centroDistribuicao().id().equals(cdId)
                        && municipio.centroDistribuicao().descricao().equals("CD Central"))
                .verifyComplete();
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("m.descricao ILIKE :descricao"));
        assertTrue(sql.getValue().contains("m.estado ILIKE :estado"));
        assertTrue(sql.getValue().contains("cd.id = :centroDistribuicao"));
        assertTrue(sql.getValue().contains("ORDER BY LOWER(cd.descricao) DESC, m.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
        verify(query).bind("descricao", "%Cidade%");
        verify(query).bind("estado", "%SP%");
        verify(query).bind("centroDistribuicao", cdId);
    }

    @Test
    void shouldCountWithTheSameFilters() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<Long> rows = mock(RowsFetchSpec.class);
        UUID cdId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("total", Long.class)).thenReturn(9L);
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenAnswer(invocation -> {
                    Long count = invocation.<BiFunction<Row, RowMetadata, Long>>getArgument(0).apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(count));
                    return rows;
                });
        var filtro = new MunicipioPaginaDTO();
        filtro.setCentroDistribuicao(cdId);

        StepVerifier.create(new MunicipioRepositoryCustomImpl(client).countByFilter(filtro))
                .expectNext(9L).verifyComplete();
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("cd.id = :centroDistribuicao"));
        verify(query).bind("centroDistribuicao", cdId);
    }

    @Test
    void shouldRejectUnsupportedSortInput() {
        var filtro = new MunicipioPaginaDTO();
        filtro.setOrdenarPor("id");
        assertThrows(IllegalArgumentException.class,
                () -> new MunicipioRepositoryCustomImpl(mock(DatabaseClient.class)).findPageByFilter(filtro, 10, 0));

        filtro.setOrdenarPor("centroDistribuicao DESC; DROP TABLE municipio");
        assertThrows(IllegalArgumentException.class,
                () -> new MunicipioRepositoryCustomImpl(mock(DatabaseClient.class)).findPageByFilter(filtro, 10, 0));
    }
}
