package com.medic.ETL.repository.estoque.interno;

import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InsercaoEstoqueInternoProdutoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final InsercaoEstoqueInternoProdutoRepository repository = new InsercaoEstoqueInternoProdutoRepository(jdbcTemplate);

    @Test
    void shouldInsertInternalStockRowsAndAllowNullQuantity() throws Exception {
        var estoque = TestDataFactory.estoqueInterno(null);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        repository.inserirEmLote(List.of(estoque));

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), setterCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("insert into estoque_interno"));
        assertEquals(1, setterCaptor.getValue().getBatchSize());

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 0);

        verify(ps).setObject(1, estoque.getProcessamento());
        verify(ps).setString(2, "UFX");
        verify(ps).setString(3, "01");
        verify(ps).setObject(4, estoque.getIdEmpresaMunicipio());
        verify(ps).setString(5, "P1");
        verify(ps).setNull(6, Types.INTEGER);
    }
}
