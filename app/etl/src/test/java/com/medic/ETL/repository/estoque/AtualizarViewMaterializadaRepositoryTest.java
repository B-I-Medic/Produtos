package com.medic.ETL.repository.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AtualizarViewMaterializadaRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final AtualizarViewMaterializadaRepository repository = new AtualizarViewMaterializadaRepository(jdbcTemplate);

    @Test
    void shouldRefreshMaterializedStockView() {
        repository.atualizar();

        verify(jdbcTemplate).execute("REFRESH MATERIALIZED VIEW mv_estoque");
    }
}
