package com.medic.ETL.repository.demanda;

import com.medic.ETL.model.demanda.Demanda;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class ConsultaDemandaRepository {

    private final JdbcTemplate ufxJdbcTemplate;

    public ConsultaDemandaRepository(@Qualifier("UFXJdbcTemplate") JdbcTemplate ufxJdbcTemplate) {
        this.ufxJdbcTemplate = ufxJdbcTemplate;
    }

    public List<Demanda> consultarUFX(String consulta) {

        return ufxJdbcTemplate.query(consulta, (rs, rowNum) -> mapper(rs));
    }

    private Demanda mapper(ResultSet rs) throws SQLException {

        Demanda demanda = new Demanda();

        demanda.setProcessamento(UUID.fromString(rs.getString("processamento")));
        demanda.setCodEmpresa(rs.getString("CodEmpresa"));
        demanda.setIbge(rs.getString("IBGE"));
        demanda.setCodProduto(rs.getString("CodProduto"));
        demanda.setQntOrcado(rs.getInt("QntOrcado"));
        demanda.setQntAprovado(rs.getInt("QntAprovado"));
        demanda.setQntAgendado(rs.getInt("QntAgendado"));
        demanda.setQntUtilizado(rs.getInt("QntUtilizado"));
        demanda.setQntTotal(rs.getInt("QntTotal"));

        return demanda;
    }
}
