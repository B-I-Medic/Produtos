package com.medic.ETL.repository.estoque.segregado;

import com.medic.ETL.model.estoque.segregado.EstoqueSegregado;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ConsultaConsultaSegregadoUFXRepository {

    private final JdbcTemplate ufxJdbcTemplate;

    public ConsultaConsultaSegregadoUFXRepository(@Qualifier("UFXJdbcTemplate") JdbcTemplate ufxJdbcTemplate) {
        this.ufxJdbcTemplate = ufxJdbcTemplate;
    }

    public List<EstoqueSegregado> consultar(String consulta) {

        return ufxJdbcTemplate.query(consulta, (rs, rowNum) -> {

            EstoqueSegregado dto = new EstoqueSegregado();

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
