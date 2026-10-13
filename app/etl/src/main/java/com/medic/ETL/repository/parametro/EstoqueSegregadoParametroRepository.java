package com.medic.ETL.repository.parametro;

import com.medic.ETL.dto.parametro.EstoqueSegregadoParametroDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EstoqueSegregadoParametroRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstoqueSegregadoParametroRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EstoqueSegregadoParametroDTO> obterEstoqueSegregadoParametros() {

        String sql = """
                select
                    e.codigo_empresa as codEmpresa,
                    es.cod_ibge as CodIBGE,
                    es.cod_segregado as codSegregado
                from estoque_segregado_parametros es
                join empresa e
                    on e.id = es.id_empresa
                where e.possui_estoque_segregado is true
                    and e.viman = 'UFX'
                order by
                    e.codigo_empresa,
                    es.cod_ibge,
                    es.cod_segregado
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            EstoqueSegregadoParametroDTO dto = new EstoqueSegregadoParametroDTO();

            dto.setCodEmpresa(rs.getString("codEmpresa"));
            dto.setCodIBGE(rs.getString("CodIBGE"));
            dto.setCodSegregado(rs.getString("codSegregado"));

            return dto;
        });
    }
}
