package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
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

class EstoqueSegregadoRepositoryCustomImplTest {

    @Test
    void shouldApplyCompanyDescriptionAndMunicipalityIdFiltersAndSortByMunicipalityName() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        var metadata = mock(RowMetadata.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<EstoqueSegregadoResponseDTO> rows = mock(RowsFetchSpec.class);
        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("id", UUID.class)).thenReturn(itemId);
        when(row.get("cod_segregado", Integer.class)).thenReturn(7);
        when(row.get("descricao", String.class)).thenReturn("Estoque especial");
        when(row.get("ativo", Boolean.class)).thenReturn(true);
        when(row.get("empresa_id", UUID.class)).thenReturn(empresaId);
        when(row.get("empresa_descricao", String.class)).thenReturn("Empresa");
        when(row.get("empresa_viman", String.class)).thenReturn("UFX");
        when(row.get("empresa_codigo_empresa", String.class)).thenReturn("001");
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Campinas");
        when(row.get("municipio_codigo_ibge", String.class)).thenReturn("123");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, EstoqueSegregadoResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    var mapper = invocation.<BiFunction<Row, RowMetadata, EstoqueSegregadoResponseDTO>>getArgument(0);
                    var item = mapper.apply(row, metadata);
                    when(rows.all()).thenReturn(Flux.just(item));
                    return rows;
                });
        var filtro = new EstoqueSegregadoPaginaDTO();
        filtro.setDescricao("Estoque especial");
        filtro.setMunicipio(municipioId);
        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC");

        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(client)
                        .findPageByEmpresaAndFilter(empresaId, filtro, 10, 20))
                .expectNextMatches(item -> item.id().equals(itemId)
                        && item.empresa().id().equals(empresaId)
                        && item.municipio().id().equals(municipioId))
                .verifyComplete();
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("esp.id_empresa = :idEmpresa"));
        assertTrue(sql.getValue().contains("LOWER(TRIM(esp.descricao)) = LOWER(TRIM(:descricao))"));
        assertTrue(sql.getValue().contains("m.id = :municipio"));
        assertTrue(sql.getValue().contains("ORDER BY LOWER(m.descricao) DESC, esp.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
        verify(query).bind("idEmpresa", empresaId);
        verify(query).bind("descricao", "Estoque especial");
        verify(query).bind("municipio", municipioId);
        verify(query).bind("limite", 10L);
        verify(query).bind("deslocamento", 20L);
    }

    @Test
    void shouldCountUsingCompanyAndFilterPredicates() {
        var client = mock(DatabaseClient.class);
        var query = mock(DatabaseClient.GenericExecuteSpec.class);
        var row = mock(Row.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<Long> rows = mock(RowsFetchSpec.class);
        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(row.get("total", Long.class)).thenReturn(6L);
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenAnswer(invocation -> {
                    Long count = invocation.<BiFunction<Row, RowMetadata, Long>>getArgument(0).apply(row, null);
                    when(rows.one()).thenReturn(Mono.just(count));
                    return rows;
                });
        var filtro = new EstoqueSegregadoPaginaDTO();
        filtro.setMunicipio(municipioId);
        StepVerifier.create(new EstoqueSegregadoRepositoryCustomImpl(client).countByEmpresaAndFilter(empresaId, filtro))
                .expectNext(6L).verifyComplete();
        verify(query).bind("idEmpresa", empresaId);
        verify(query).bind("municipio", municipioId);
    }

    @Test
    void shouldRejectUnsupportedSortField() {
        var filtro = new EstoqueSegregadoPaginaDTO();
        filtro.setOrdenarPor("municipio DESC; DROP TABLE empresa");
        assertThrows(IllegalArgumentException.class,
                () -> new EstoqueSegregadoRepositoryCustomImpl(mock(DatabaseClient.class))
                        .findPageByEmpresaAndFilter(UUID.randomUUID(), filtro, 10, 0));
    }
}
