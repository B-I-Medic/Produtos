package com.medic.ETL.repository.produto;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultaProdutoRepositoryTest {

    private final JdbcTemplate s00JdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcTemplate ufxJdbcTemplate = mock(JdbcTemplate.class);
    private final ConsultaProdutoRepository repository = new ConsultaProdutoRepository(s00JdbcTemplate, ufxJdbcTemplate);

    @SuppressWarnings("unchecked")
    @Test
    void shouldMapSourceLocalDateTimeToInstantInSaoPauloTimeZone() throws Exception {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("CriadoEm")).thenReturn("2026-08-10 10:00:00");
        when(ufxJdbcTemplate.query(eq("consulta"), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<?> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });

        var produto = repository.consultarUFX("consulta").getFirst();

        assertEquals(Instant.parse("2026-08-10T13:00:00Z"), produto.getCriadoEm());
    }
}
