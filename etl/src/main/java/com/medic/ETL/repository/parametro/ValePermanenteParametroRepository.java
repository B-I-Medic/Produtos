package com.medic.ETL.repository.parametro;

import com.medic.ETL.dto.parametro.ValePermanenteParametroDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ValePermanenteParametroRepository {

    private final JdbcTemplate jdbcTemplate;

    public ValePermanenteParametroRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ValePermanenteParametroDTO> obterValePermanenteParametros() {

        String sql = """
                select
                    vp."compor_subCd" as "subCd",
                    vp.cod_vp as "codVp",
                    e.viman as "viman",
                    e.codigo_empresa as "codEmpresa"
                from vale_permanente_parametros vp
                join empresa e
                    on e.id = vp.id_empresa
                order by
                    e.viman,
                    vp."compor_subCd",
                    e.codigo_empresa,
                    vp.cod_vp
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            ValePermanenteParametroDTO dto = new ValePermanenteParametroDTO();

            dto.setSubCd(rs.getObject("subCd", UUID.class));
            dto.setCodVp(rs.getString("codVp"));
            dto.setViman(rs.getString("viman"));
            dto.setCodEmpresa(rs.getString("codEmpresa"));

            return dto;
        });
    }
}
