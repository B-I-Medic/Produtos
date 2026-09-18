package com.medic.ETL.repository.processamento;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProcessamentoCustomRepositoryImplTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ProcessamentoCustomRepositoryImpl repository = new ProcessamentoCustomRepositoryImpl(jdbcTemplate);

    @Test
    void shouldDeleteOnlyProcessingRowsOlderThanBusinessCutoff() {
        repository.excluirProcessamentosAntigos();

        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains("America/Sao_Paulo"));
    }
}
