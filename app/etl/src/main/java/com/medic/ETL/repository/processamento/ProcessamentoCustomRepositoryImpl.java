package com.medic.ETL.repository.processamento;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessamentoCustomRepositoryImpl implements ProcessamentoCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProcessamentoCustomRepositoryImpl(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean lockEmUso(long lockKey) {

        Boolean lockAdquirido = jdbcTemplate.queryForObject(
                "select pg_try_advisory_lock(?)",
                Boolean.class,
                lockKey
        );

        return !Boolean.TRUE.equals(lockAdquirido);
    }

    public void liberarLock(long lockKey) {

        jdbcTemplate.queryForObject("select pg_advisory_unlock(?)", Boolean.class, lockKey);
    }
}
