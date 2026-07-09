package com.medic.ETL.repository.estoque;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AtualizarViewMaterializadaRepository {

    private final JdbcTemplate jdbcTemplate;

    public AtualizarViewMaterializadaRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void atualizar() {

        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW mv_estoque");
    }
}
