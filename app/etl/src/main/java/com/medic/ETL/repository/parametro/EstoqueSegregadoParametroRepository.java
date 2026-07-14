package com.medic.ETL.repository.parametro;

import com.medic.ETL.dto.parametro.EstoqueSegregadoParametroDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class EstoqueSegregadoParametroRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstoqueSegregadoParametroRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EstoqueSegregadoParametroDTO> obterEstoqueSegregadoParametros() {

        String sql = """
                select
                    es.id_empresa_municipio as id_empresa_municipio,
                    es.cod_segregado as "codSegregado",
                    e.viman as "viman",
                    e.codigo_empresa as "codEmpresa"
                from estoque_segregado_parametros es
                join empresa e
                    on e.id = es.id_empresa
                order by
                    es.id_empresa_municipio,
                    e.codigo_empresa,
                    es.cod_segregado
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            EstoqueSegregadoParametroDTO dto = new EstoqueSegregadoParametroDTO();

            dto.setIdEmpresaMunicipio(rs.getObject("id_empresa_municipio", UUID.class));
            dto.setCodSegregado(rs.getString("codSegregado"));
            dto.setViman(rs.getString("viman"));
            dto.setCodEmpresa(rs.getString("codEmpresa"));

            return dto;
        });
    }
}
