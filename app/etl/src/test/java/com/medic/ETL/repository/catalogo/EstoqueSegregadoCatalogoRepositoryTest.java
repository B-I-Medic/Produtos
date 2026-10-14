package com.medic.ETL.repository.catalogo;

import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoDTO;
import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EstoqueSegregadoCatalogoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final EstoqueSegregadoCatalogoRepository repository = new EstoqueSegregadoCatalogoRepository(jdbcTemplate);

    @Test
    void shouldReadActiveCatalogRowsAndMapAllFields() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("codEmpresa")).thenReturn("07");
        when(resultSet.getString("viman")).thenReturn("UFX");
        when(resultSet.getString("CodIBGE")).thenReturn("3550308");
        when(resultSet.getString("codSegregado")).thenReturn("42");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });

        List<EstoqueSegregadoCatalogoDTO> itens = repository.obterEstoqueSegregadoCatalogo();

        assertEquals("07", itens.getFirst().getCodEmpresa());
        assertEquals("UFX", itens.getFirst().getViman());
        assertEquals("3550308", itens.getFirst().getCodIBGE());
        assertEquals("42", itens.getFirst().getCodSegregado());
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(RowMapper.class));
        assertTrue(sql.getValue().contains("from estoque_segregado_catalogo es"));
        assertTrue(sql.getValue().contains("es.ativo is true"));
    }

    @Test
    void shouldUpsertItemsAndDeleteCodesMissingFromSnapshot() throws Exception {
        UUID idEmpresa = UUID.randomUUID();
        List<EstoqueSegregadoCatalogoItemDTO> itens = List.of(
                new EstoqueSegregadoCatalogoItemDTO(idEmpresa, 10, "Material A"),
                new EstoqueSegregadoCatalogoItemDTO(idEmpresa, 20, "Material B")
        );
        Map<UUID, List<EstoqueSegregadoCatalogoItemDTO>> snapshot = new LinkedHashMap<>();
        snapshot.put(idEmpresa, itens);

        repository.reconciliar(snapshot);

        var setterCaptor = org.mockito.ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        var updateSqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var updateArgsCaptor = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).batchUpdate(updateSqlCaptor.capture(), setterCaptor.capture());
        verify(jdbcTemplate).update(updateSqlCaptor.capture(), updateArgsCaptor.capture());
        assertTrue(updateSqlCaptor.getAllValues().getFirst().contains("ON CONFLICT (id_empresa, cod_segregado)"));
        assertEquals(2, setterCaptor.getValue().getBatchSize());
        assertTrue(updateSqlCaptor.getAllValues().getLast().contains("cod_segregado NOT IN (?, ?)"));
        assertEquals(idEmpresa, updateArgsCaptor.getValue()[0]);
        assertEquals(10, updateArgsCaptor.getValue()[1]);
        assertEquals(20, updateArgsCaptor.getValue()[2]);

        PreparedStatement statement = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(statement, 1);
        verify(statement).setObject(2, idEmpresa);
        verify(statement).setInt(3, 20);
        verify(statement).setString(4, "Material B");
    }

    @Test
    void shouldDeleteAllCodesForEmptyCompanySnapshotAndIgnoreEmptySnapshots() {
        UUID idEmpresa = UUID.randomUUID();

        repository.reconciliar(Map.of(idEmpresa, List.of()));

        verify(jdbcTemplate).update("DELETE FROM estoque_segregado_catalogo WHERE id_empresa = ?", idEmpresa);
        org.mockito.Mockito.verifyNoMoreInteractions(jdbcTemplate);

        org.mockito.Mockito.reset(jdbcTemplate);
        repository.reconciliar(Map.of());
        org.mockito.Mockito.verifyNoInteractions(jdbcTemplate);
    }
}
