package com.medic.ETL.repository.catalogo;

import com.medic.ETL.dto.catalogo.EstoqueSegregadoCatalogoItemDTO;
import com.medic.ETL.model.empresa.EmpresaModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EstoqueSegregadoCatalogoOrigemRepository {

    private final JdbcTemplate ufxJdbcTemplate;
    private final JdbcTemplate s00JdbcTemplate;

    public EstoqueSegregadoCatalogoOrigemRepository(@Qualifier("UFXJdbcTemplate") JdbcTemplate ufxJdbcTemplate,
                                                     @Qualifier("S00JdbcTemplate") JdbcTemplate s00JdbcTemplate) {
        this.ufxJdbcTemplate = ufxJdbcTemplate;
        this.s00JdbcTemplate = s00JdbcTemplate;
    }

    public List<EstoqueSegregadoCatalogoItemDTO> consultarUfx(EmpresaModel empresa) {

        String codigoEmpresa = validarCodigoEmpresa(empresa.getCodigoEmpresa());
        String sufixoVetees = codigoEmpresa;
        String sufixoVetenc = sufixoVetencUfx(Integer.parseInt(codigoEmpresa));

        return consultar(ufxJdbcTemplate, empresa, sufixoVetees, sufixoVetenc);
    }

    public List<EstoqueSegregadoCatalogoItemDTO> consultarS00(EmpresaModel empresa) {

        String codigoEmpresa = validarCodigoEmpresa(empresa.getCodigoEmpresa());
        int codigoNumerico = Integer.parseInt(codigoEmpresa);
        String sufixoVetenc = String.format("%02d", Math.floorMod(codigoNumerico - 1, 20) + 1);

        return consultar(s00JdbcTemplate, empresa, codigoEmpresa, sufixoVetenc);
    }

    private List<EstoqueSegregadoCatalogoItemDTO> consultar(JdbcTemplate jdbcTemplate,
                                                            EmpresaModel empresa,
                                                            String sufixoVetees,
                                                            String sufixoVetenc) {

        String sql = """
                SELECT
                    ES.ESCDNC AS CODIGO,
                    NC.NCDESC AS DESCRICAO
                FROM SYSADM.VETEES%s ES
                JOIN SYSADM.VETENC%s NC
                    ON ES.ESCDNC = NC.NCCODI
                GROUP BY ES.ESCDNC, NC.NCDESC
                """.formatted(sufixoVetees, sufixoVetenc);

        return jdbcTemplate.query(sql, (rs, rowNum) -> new EstoqueSegregadoCatalogoItemDTO(
                empresa.getId(),
                rs.getInt("CODIGO"),
                rs.getString("DESCRICAO").trim()
        ));
    }

    private String validarCodigoEmpresa(String codigoEmpresa) {

        if (codigoEmpresa == null || !codigoEmpresa.trim().matches("\\d{1,2}")) {
            throw new IllegalArgumentException("Codigo de empresa invalido para consulta de estoque segregado: " + codigoEmpresa);
        }

        return codigoEmpresa.trim();
    }

    private String sufixoVetencUfx(int codigoEmpresa) {

        return switch (codigoEmpresa) {
            case 1, 3, 4, 5, 6, 13 -> "01";
            case 7, 8 -> String.format("%02d", codigoEmpresa);
            case 11 -> "10";
            default -> throw new IllegalArgumentException(
                    "Nao ha mapeamento de VETENC para a empresa UFX " + String.format("%02d", codigoEmpresa)
            );
        };
    }
}
