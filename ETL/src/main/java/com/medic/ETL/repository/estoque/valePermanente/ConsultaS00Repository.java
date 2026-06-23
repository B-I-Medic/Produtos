package com.medic.ETL.repository.estoque.valePermanente;

import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConsultaS00Repository {

    private final JdbcTemplate s00JdbcTemplate;

    public ConsultaS00Repository(@Qualifier("S00JdbcTemplate") JdbcTemplate s00JdbcTemplate) {
        this.s00JdbcTemplate = s00JdbcTemplate;
    }

    public List<ValePermanente> consultar(String consulta) {

        return s00JdbcTemplate.query(consulta, (rs, rowNum) -> {

            ValePermanente dto = new ValePermanente();

            dto.setProcessamento(rs.getObject("Processamento", UUID.class));
            dto.setViman(rs.getString("Viman"));
            dto.setCodEmpresa(rs.getString("CodEmpresa"));
            dto.setIdEmpresaMunicipio(rs.getObject("IdEmpresaMunicipio", UUID.class));
            dto.setCodProduto(rs.getString("CodProduto"));
            dto.setQntDisponivel(rs.getInt("QntDisponivel"));

            return dto;
        });
    }
}
