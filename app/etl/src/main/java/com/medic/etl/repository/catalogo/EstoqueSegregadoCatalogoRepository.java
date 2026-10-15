package com.medic.etl.repository.catalogo;

import com.medic.etl.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import com.medic.etl.dto.catalogo.EstoqueSegregadoCatalogoDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class EstoqueSegregadoCatalogoRepository {

    private static final String UPSERT = """
            INSERT INTO estoque_segregado_catalogo (
                id, id_empresa, cod_segregado, descricao, ativo
            ) VALUES (?, ?, ?, ?, false)
            ON CONFLICT (id_empresa, cod_segregado)
            DO UPDATE SET
                descricao = EXCLUDED.descricao,
                atualizado_em = now()
            WHERE estoque_segregado_catalogo.descricao IS DISTINCT FROM EXCLUDED.descricao
            """;

    private final JdbcTemplate jdbcTemplate;

    public EstoqueSegregadoCatalogoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EstoqueSegregadoCatalogoDTO> obterEstoqueSegregadoCatalogo() {

        String sql = """
                select
                    e.codigo_empresa as codEmpresa,
                    e.viman as viman,
                    es.cod_ibge as CodIBGE,
                    es.cod_segregado as codSegregado
                from estoque_segregado_catalogo es
                join empresa e
                    on e.id = es.id_empresa
                where e.possui_estoque_segregado is true
                    and es.ativo is true
                    and es.cod_ibge is not null
                order by
                    e.viman,
                    e.codigo_empresa,
                    es.cod_ibge,
                    es.cod_segregado
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            EstoqueSegregadoCatalogoDTO dto = new EstoqueSegregadoCatalogoDTO();

            dto.setCodEmpresa(rs.getString("codEmpresa"));
            dto.setViman(rs.getString("viman"));
            dto.setCodIBGE(rs.getString("CodIBGE"));
            dto.setCodSegregado(rs.getString("codSegregado"));

            return dto;
        });
    }

    @Transactional
    public void reconciliar(Map<UUID, List<EstoqueSegregadoCatalogoItemDTO>> snapshotPorEmpresa) {

        for (Map.Entry<UUID, List<EstoqueSegregadoCatalogoItemDTO>> empresa : snapshotPorEmpresa.entrySet()) {
            List<EstoqueSegregadoCatalogoItemDTO> itens = empresa.getValue();

            if (!itens.isEmpty()) {
                jdbcTemplate.batchUpdate(UPSERT, new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        EstoqueSegregadoCatalogoItemDTO item = itens.get(index);
                        ps.setObject(1, UUID.randomUUID());
                        ps.setObject(2, empresa.getKey());
                        ps.setInt(3, item.codSegregado());
                        ps.setString(4, item.descricao());
                    }

                    @Override
                    public int getBatchSize() {
                        return itens.size();
                    }
                });
            }

            excluirAusentes(empresa.getKey(), itens);
        }
    }

    private void excluirAusentes(UUID idEmpresa, List<EstoqueSegregadoCatalogoItemDTO> itens) {

        if (itens.isEmpty()) {
            jdbcTemplate.update("DELETE FROM estoque_segregado_catalogo WHERE id_empresa = ?", idEmpresa);
            return;
        }

        String placeholders = String.join(", ", java.util.Collections.nCopies(itens.size(), "?"));
        String sql = "DELETE FROM estoque_segregado_catalogo " +
                "WHERE id_empresa = ? AND cod_segregado NOT IN (" + placeholders + ")";
        List<Object> valoresSql = new ArrayList<>(itens.size() + 1);
        valoresSql.add(idEmpresa);
        itens.stream().map(EstoqueSegregadoCatalogoItemDTO::codSegregado).forEach(valoresSql::add);
        jdbcTemplate.update(sql, valoresSql.toArray());
    }
}
