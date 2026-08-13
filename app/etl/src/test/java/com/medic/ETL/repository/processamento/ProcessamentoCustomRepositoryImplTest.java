package com.medic.ETL.repository.processamento;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessamentoCustomRepositoryImplTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ProcessamentoCustomRepositoryImpl repository = new ProcessamentoCustomRepositoryImpl(jdbcTemplate);

    @Test
    void shouldReturnFalseWhenAdvisoryLockIsAcquired() {
        when(jdbcTemplate.queryForObject("select pg_try_advisory_lock(?)", Boolean.class, 10L))
                .thenReturn(true);

        assertFalse(repository.lockEmUso(10L));
    }

    @Test
    void shouldReturnTrueWhenAdvisoryLockIsUnavailable() {
        when(jdbcTemplate.queryForObject("select pg_try_advisory_lock(?)", Boolean.class, 10L))
                .thenReturn(false);

        assertTrue(repository.lockEmUso(10L));
    }

    @Test
    void shouldReleaseAdvisoryLock() {
        repository.liberarLock(10L);

        verify(jdbcTemplate).queryForObject("select pg_advisory_unlock(?)", Boolean.class, 10L);
    }

    @Test
    void shouldDeleteOnlyProcessingRowsOlderThanBusinessCutoff() {
        repository.excluirProcessamentosAntigos();

        verify(jdbcTemplate).execute(org.mockito.ArgumentMatchers.contains("America/Sao_Paulo"));
    }
}
