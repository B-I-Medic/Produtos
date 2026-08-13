package com.medic.ETL.repository.produto;

import com.medic.ETL.model.produto.Produto;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.List;

@Repository
public class InsercaoProdutoProdutoRepository {

    private final JdbcTemplate jdbcTemplate;

    public InsercaoProdutoProdutoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inserirOuAtualizarEmLote(List<Produto> itens) {

        String sql = """
                insert into produto (
                    viman,
                    cod_empresa,
                    cod_produto,
                    descricao,
                    marca,
                    tipo,
                    anvisa,
                    situacao,
                    criado_por,
                    criado_em
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (viman, cod_empresa, cod_produto) do update set
                    descricao = excluded.descricao,
                    marca = excluded.marca,
                    tipo = excluded.tipo,
                    anvisa = excluded.anvisa,
                    situacao = excluded.situacao,
                    criado_por = excluded.criado_por,
                    criado_em = excluded.criado_em
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(@NonNull PreparedStatement ps, int index) throws SQLException {

                Produto item = itens.get(index);

                ps.setString(1, item.getViman());
                ps.setString(2, item.getCodEmpresa());
                ps.setString(3, item.getCodProduto());
                ps.setString(4, item.getDescricao());
                ps.setString(5, item.getMarca());
                ps.setString(6, item.getTipo());

                if (item.getAnvisa() == null) {
                    ps.setNull(7, Types.INTEGER);
                } else {
                    ps.setLong(7, item.getAnvisa());
                }

                ps.setString(8, item.getSituacao());
                ps.setString(9, item.getCriadoPor());

                Instant criadoEm = item.getCriadoEm();

                if (criadoEm == null) {
                    ps.setNull(10, Types.TIMESTAMP_WITH_TIMEZONE);
                } else {
                    ps.setObject(10, criadoEm, Types.TIMESTAMP_WITH_TIMEZONE);
                }
            }

            @Override
            public int getBatchSize() {
                return itens.size();
            }
        });
    }
}
