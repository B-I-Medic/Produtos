package com.medic.ETL.repository.parametro;

import com.medic.ETL.dto.parametro.EstoqueInternoParametroDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class EstoqueInternoParametroRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstoqueInternoParametroRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EstoqueInternoParametroDTO> obterEstoqueInternoParametros() {

        String sql = """
                select
                    ei."compor_subCd" as "subCd",
                    e.viman as viman,
                    e.codigo_empresa as codEmpresa
                from estoque_interno_parametros ei
                join empresa e
                    on e.id = ei.id_empresa
                order by
                    ei."compor_subCd",
                    e.viman,
                    e.codigo_empresa
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            EstoqueInternoParametroDTO dto = new EstoqueInternoParametroDTO();

            dto.setSubCd(rs.getObject("subCd", UUID.class));
            dto.setViman(rs.getString("viman"));
            dto.setCodEmpresa(rs.getString("codEmpresa"));

            return dto;
        });
    }
}
