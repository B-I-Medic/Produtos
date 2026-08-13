package com.medic.ETL.repository.produto;

import com.medic.ETL.model.produto.Produto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

@Repository
public class ConsultaProdutoRepository {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId SOURCE_TIME_ZONE = ZoneId.of("America/Sao_Paulo");

    private final JdbcTemplate s00JdbcTemplate;
    private final JdbcTemplate ufxJdbcTemplate;

    public ConsultaProdutoRepository(@Qualifier("S00JdbcTemplate") JdbcTemplate s00JdbcTemplate,
                                     @Qualifier("UFXJdbcTemplate") JdbcTemplate ufxJdbcTemplate) {
        this.s00JdbcTemplate = s00JdbcTemplate;
        this.ufxJdbcTemplate = ufxJdbcTemplate;
    }

    public List<Produto> consultarS00(String consulta) {

        return s00JdbcTemplate.query(consulta, (rs, rowNum) -> mapper(rs));
    }

    public List<Produto> consultarUFX(String consulta) {

        return ufxJdbcTemplate.query(consulta, (rs, rowNum) -> mapper(rs));
    }

    private Produto mapper(ResultSet rs) throws SQLException {

        Produto produto = new Produto();

        produto.setViman(rs.getString("Viman"));
        produto.setCodEmpresa(rs.getString("CodEmpresa"));
        produto.setCodProduto(rs.getString("CodProduto"));
        produto.setDescricao(rs.getString("Descricao"));
        produto.setMarca(rs.getString("Marca"));
        produto.setTipo(rs.getString("Tipo"));
        produto.setSituacao(rs.getString("Situacao"));
        produto.setCriadoPor(rs.getString("CriadoPor"));

        String anvisa = rs.getString("Anvisa");

        if (anvisa != null && !anvisa.isEmpty()) {

            produto.setAnvisa(Long.parseLong(anvisa));

        } else {

            produto.setAnvisa(null);
        }

        String criadoEm = rs.getString("CriadoEm");

        if (criadoEm != null && !criadoEm.isBlank()) {

            LocalDateTime criadoEmLocal = LocalDateTime.parse(criadoEm.trim(), formatter);
            produto.setCriadoEm(criadoEmLocal.atZone(SOURCE_TIME_ZONE).toInstant());

        } else {
            produto.setCriadoEm(null);
        }

        return produto;
    }
}
