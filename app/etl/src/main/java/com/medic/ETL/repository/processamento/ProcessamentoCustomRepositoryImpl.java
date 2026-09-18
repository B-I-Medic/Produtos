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

    public void excluirProcessamentosAntigos() {

        jdbcTemplate.execute("""
                delete from processamento p
                where p.concluido_em < (
                    (CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date
                    - INTERVAL '1 day'
                    + TIME '07:00:00'
                    ) AT TIME ZONE 'America/Sao_Paulo'
                    and not exists (
                        select 1
                        from anvisa_carga ac
                        where ac.processamento_id = p.id
                          and ac.ativa = true
                    )
                    and p.id not in (
                        select latest.id
                        from (
                            select distinct on (entidade) id
                            from processamento
                            order by entidade, concluido_em desc nulls last, iniciado_em desc
                        ) latest
                    )
                """);
    }
}
