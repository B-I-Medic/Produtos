package com.medic.ETL.repository.estoque.valePermanente;

import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConsultaUFXRepository {

    private final JdbcTemplate ufxJdbcTemplate;

    public ConsultaUFXRepository(@Qualifier("UFXJdbcTemplate") JdbcTemplate ufxJdbcTemplate) {
        this.ufxJdbcTemplate = ufxJdbcTemplate;
    }

    public List<ValePermanente> consultar(String consulta) {

        return ufxJdbcTemplate.query(consulta, (rs, rowNum) -> {

            ValePermanente dto = new ValePermanente();

            dto.setProcessamento(UUID.fromString(rs.getString("Processamento")));
            dto.setViman(rs.getString("Viman"));
            dto.setCodEmpresa(rs.getString("CodEmpresa"));
            dto.setIdEmpresaMunicipio(UUID.fromString(rs.getString("IdEmpresaMunicipio")));
            dto.setCodProduto(rs.getString("CodProduto"));
            dto.setQntDisponivel(rs.getInt("QntDisponivel"));

            return dto;
        });
    }
}
