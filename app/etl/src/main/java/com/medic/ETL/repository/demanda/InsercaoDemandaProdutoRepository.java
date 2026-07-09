package com.medic.ETL.repository.demanda;

import com.medic.ETL.model.demanda.Demanda;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InsercaoDemandaProdutoRepository {

    private final JdbcTemplate jdbcTemplate;

    public InsercaoDemandaProdutoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inserirEmLote(List<Demanda> itens) {

        String sql = """
                insert into demanda (
                    processamento,
                    cod_empresa,
                    cod_ibge,
                    cod_produto,
                    qnt_orcado,
                    qnt_aprovado,
                    qnt_agendado,
                    qnt_utilizado,
                    qnt_total
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(@NonNull PreparedStatement ps, int index) throws SQLException {

                Demanda item = itens.get(index);

                ps.setObject(1, item.getProcessamento());
                ps.setString(2, item.getCodEmpresa());
                ps.setString(3, item.getIbge());
                ps.setString(4, item.getCodProduto());

                ps.setInt(5, item.getQntOrcado());
                ps.setInt(6, item.getQntAprovado());
                ps.setInt(7, item.getQntAgendado());
                ps.setInt(8, item.getQntUtilizado());
                ps.setInt(9, item.getQntTotal());
            }

            @Override
            public int getBatchSize() {
                return itens.size();
            }
        });
    }
}
