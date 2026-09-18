package com.medic.ETL.repository.anvisa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnvisaConfiguracaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnvisaConfiguracaoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getRetryCooldownMinutos() {
        Integer value = jdbcTemplate.queryForObject(
                "select retry_cooldown_minutos from anvisa_configuracao where id = 1",
                Integer.class
        );

        // CoolDown hardcode
        return value == null ? 5 : value;
    }
}
