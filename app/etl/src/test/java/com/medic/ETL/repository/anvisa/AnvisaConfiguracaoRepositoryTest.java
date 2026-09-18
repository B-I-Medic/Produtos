package com.medic.ETL.repository.anvisa;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnvisaConfiguracaoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final AnvisaConfiguracaoRepository repository = new AnvisaConfiguracaoRepository(jdbcTemplate);

    @Test
    void shouldReturnConfiguredCooldown() {

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(12);

        assertEquals(12, repository.getRetryCooldownMinutos());
    }

    @Test
    void shouldUseDefaultCooldownWhenDatabaseReturnsNull() {

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(null);

        assertEquals(5, repository.getRetryCooldownMinutos());
    }
}
