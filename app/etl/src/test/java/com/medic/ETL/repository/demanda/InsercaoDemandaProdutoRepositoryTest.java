package com.medic.ETL.repository.demanda;

import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InsercaoDemandaProdutoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final InsercaoDemandaProdutoRepository repository = new InsercaoDemandaProdutoRepository(jdbcTemplate);

    @Test
    void shouldInsertDemandRowsWithAllQuantities() throws Exception {
        var demanda = TestDataFactory.demanda();
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        repository.inserirEmLote(List.of(demanda));

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), setterCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("insert into demanda"));
        assertEquals(1, setterCaptor.getValue().getBatchSize());

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 0);

        verify(ps).setObject(1, demanda.getProcessamento());
        verify(ps).setString(2, "01");
        verify(ps).setString(3, "3550308");
        verify(ps).setString(4, "P1");
        verify(ps).setInt(5, 1);
        verify(ps).setInt(9, 10);
    }
}
