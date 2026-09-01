package com.medic.ETL.repository.produto;

import com.medic.ETL.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InsercaoProdutoProdutoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final InsercaoProdutoProdutoRepository repository = new InsercaoProdutoProdutoRepository(jdbcTemplate);

    @Test
    void shouldUpsertProductRowsWithNullableFields() throws Exception {
        var produto = TestDataFactory.produto("UFX", "01", "P1");
        produto.setAnvisa(null);
        produto.setCriadoEm(null);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        repository.inserirOuAtualizarEmLote(List.of(produto));

        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), setterCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("on conflict (viman, cod_empresa, cod_produto) do update"));
        assertEquals(1, setterCaptor.getValue().getBatchSize());

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 0);

        verify(ps).setString(1, "UFX");
        verify(ps).setString(2, "01");
        verify(ps).setString(3, "P1");
        verify(ps).setNull(7, Types.INTEGER);
        verify(ps).setNull(10, Types.TIMESTAMP_WITH_TIMEZONE);
    }

    @Test
    void shouldBindCreatedAtAsOffsetDateTime() throws Exception {

        var produto = TestDataFactory.produto("UFX", "01", "P1");
        Instant criadoEm = Instant.parse("2026-08-18T19:03:15Z");
        produto.setCriadoEm(criadoEm);
        ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);

        repository.inserirOuAtualizarEmLote(List.of(produto));

        verify(jdbcTemplate).batchUpdate(anyString(), setterCaptor.capture());

        PreparedStatement ps = mock(PreparedStatement.class);
        setterCaptor.getValue().setValues(ps, 0);

        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);
        verify(ps).setObject(eq(10), valueCaptor.capture(), eq(Types.TIMESTAMP_WITH_TIMEZONE));

        OffsetDateTime boundValue = assertInstanceOf(OffsetDateTime.class, valueCaptor.getValue());
        assertEquals(criadoEm, boundValue.toInstant());
        assertEquals(ZoneOffset.UTC, boundValue.getOffset());
    }
}
