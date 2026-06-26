package com.medic.ETL.repository.estoque.valePermanente;

import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
public class InsercaoValePermanenteProdutoRepository {

    private final JdbcTemplate jdbcTemplate;

    public InsercaoValePermanenteProdutoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inserirEmLote(List<ValePermanente> itens) {

        String sql = """
                insert into vale_permanente (
                    processamento,
                    viman,
                    cod_empresa,
                    id_empresa_municipio,
                    cod_produto,
                    qnt_disponivel
                ) values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {

                ValePermanente item = itens.get(index);

                ps.setObject(1, item.getProcessamento());
                ps.setString(2, item.getViman());
                ps.setString(3, item.getCodEmpresa());
                ps.setObject(4, item.getIdEmpresaMunicipio());
                ps.setString(5, item.getCodProduto());

                if (item.getQntDisponivel() == null) {
                    ps.setNull(6, Types.INTEGER);
                    return;
                }

                ps.setInt(6, item.getQntDisponivel());
            }

            @Override
            public int getBatchSize() {
                return itens.size();
            }
        });
    }
}
