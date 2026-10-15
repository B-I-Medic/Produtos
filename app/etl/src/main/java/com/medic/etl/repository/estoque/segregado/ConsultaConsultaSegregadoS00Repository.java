package com.medic.etl.repository.estoque.segregado;

import com.medic.etl.model.estoque.segregado.EstoqueSegregado;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConsultaConsultaSegregadoS00Repository {

    private final JdbcTemplate s00JdbcTemplate;

    public ConsultaConsultaSegregadoS00Repository(@Qualifier("S00JdbcTemplate") JdbcTemplate s00JdbcTemplate) {
        this.s00JdbcTemplate = s00JdbcTemplate;
    }

    public List<EstoqueSegregado> consultar(String consulta) {

        return s00JdbcTemplate.query(consulta, (rs, rowNum) -> {

            EstoqueSegregado dto = new EstoqueSegregado();

            dto.setProcessamento(UUID.fromString(rs.getString("Processamento")));
            dto.setViman(rs.getString("Viman"));
            dto.setCodEmpresa(rs.getString("CodEmpresa"));
            dto.setCodIBGE(rs.getString("CodIBGE"));
            dto.setCodProduto(rs.getString("CodProduto"));
            dto.setQntDisponivel(rs.getInt("QntDisponivel"));

            return dto;
        });
    }
}
