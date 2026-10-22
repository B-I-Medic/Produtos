package com.medic.web.repository.empresa;

import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.model.empresa.Viman;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public class EmpresaRepositoryCustomImpl implements EmpresaRepositoryCustom {

    private static final String FIND_PAGE_SQL = """
            SELECT e.id AS empresa_id,
                   e.descricao AS empresa_descricao,
                   e.viman AS empresa_viman,
                   e.codigo_empresa AS empresa_codigo_empresa,
                   e.possui_estoque_interno AS empresa_possui_estoque_interno,
                   e.possui_estoque_segregado AS empresa_possui_estoque_segregado,
                   e.possui_vp AS empresa_possui_vp,
                   m.id AS municipio_id,
                   m.descricao AS municipio_descricao,
                   m.cod_ibge AS municipio_cod_ibge,
                   m.estado AS municipio_estado
              FROM empresa e
              LEFT JOIN municipio m
                ON m.id = e.municipio_id
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*) AS total
              FROM empresa e
            """;

    private static final Map<String, String> ORDER_BY_COLUMNS = Map.of(
            "descricao", "LOWER(e.descricao)",
            "codigo", "LOWER(e.codigo_empresa)",
            "viman", "e.viman",
            "possui_estoque_interno", "e.possui_estoque_interno",
            "possui_estoque_segregado", "e.possui_estoque_segregado",
            "possui_vp", "e.possui_vp",
            "municipio", "LOWER(m.descricao)"
    );

    private final DatabaseClient databaseClient;

    public EmpresaRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(UUID idEmpresa) {

        var sql = """
                SELECT exists( select 1
                               FROM empresa
                               WHERE id = :idEmpresa AND possui_estoque_segregado = true)
                """;

        var query = databaseClient
                .sql(sql)
                .bind("idEmpresa", idEmpresa);

        return query.map((row, metaData) -> Objects.requireNonNull(row.get(0, Boolean.class))).one();
    }

    @Override
    public Flux<EmpresaResponseDTO> findPageByFilter(EmpresaPaginaDTO filtro, long limite, long deslocamento) {

        String sql = FIND_PAGE_SQL + where(filtro)
                + " ORDER BY " + orderBy(filtro)
                + " LIMIT :limite OFFSET :deslocamento";

        var query = bindFilters(databaseClient.sql(sql), filtro)
                .bind("limite", limite)
                .bind("deslocamento", deslocamento);

        return query.map((row, metadata) -> mapEmpresa(row)).all();
    }

    @Override
    public Mono<Long> countByFilter(EmpresaPaginaDTO filtro) {

        String sql = COUNT_SQL + where(filtro);

        return bindFilters(databaseClient.sql(sql), filtro)
                .map((row, metadata) -> Objects.requireNonNull(row.get("total", Long.class)))
                .one();
    }

    private EmpresaResponseDTO mapEmpresa(Row row) {

        UUID municipioId = row.get("municipio_id", UUID.class);
        String vimanValue = row.get("empresa_viman", String.class);

        MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                municipioId,
                row.get("municipio_descricao", String.class),
                row.get("municipio_cod_ibge", String.class),
                row.get("municipio_estado", String.class)
        );

        return new EmpresaResponseDTO(
                row.get("empresa_id", UUID.class),
                row.get("empresa_descricao", String.class),
                municipio,
                vimanValue == null ? null : Viman.valueOf(vimanValue),
                row.get("empresa_codigo_empresa", String.class),
                Boolean.TRUE.equals(row.get("empresa_possui_estoque_interno", Boolean.class)),
                Boolean.TRUE.equals(row.get("empresa_possui_estoque_segregado", Boolean.class)),
                Boolean.TRUE.equals(row.get("empresa_possui_vp", Boolean.class))
        );
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec query,
                                                          EmpresaPaginaDTO filtro) {

        if (filtro == null) {
            return query;
        }

        if (filtro.getDescricao() != null) {
            query = query.bind("descricao", filtro.getDescricao());
        }
        if (filtro.getCodigo() != null) {
            query = query.bind("codigo", filtro.getCodigo());
        }
        if (filtro.getViman() != null) {
            query = query.bind("viman", filtro.getViman().name());
        }
        if (filtro.getPossuiEstoqueInterno() != null) {
            query = query.bind("possuiEstoqueInterno", filtro.getPossuiEstoqueInterno());
        }
        if (filtro.getPossuiEstoqueSegregado() != null) {
            query = query.bind("possuiEstoqueSegregado", filtro.getPossuiEstoqueSegregado());
        }
        if (filtro.getPossuiVp() != null) {
            query = query.bind("possuiVp", filtro.getPossuiVp());
        }
        if (filtro.getMunicipio() != null) {
            query = query.bind("municipio", filtro.getMunicipio());
        }

        return query;
    }

    private String where(EmpresaPaginaDTO filtro) {

        if (filtro == null) {
            return "";
        }

        List<String> conditions = new ArrayList<>();
        if (filtro.getDescricao() != null) {
            conditions.add("LOWER(TRIM(e.descricao)) = LOWER(TRIM(:descricao))");
        }
        if (filtro.getCodigo() != null) {
            conditions.add("LOWER(TRIM(e.codigo_empresa)) = LOWER(TRIM(:codigo))");
        }
        if (filtro.getViman() != null) {
            conditions.add("e.viman = :viman");
        }
        if (filtro.getPossuiEstoqueInterno() != null) {
            conditions.add("e.possui_estoque_interno = :possuiEstoqueInterno");
        }
        if (filtro.getPossuiEstoqueSegregado() != null) {
            conditions.add("e.possui_estoque_segregado = :possuiEstoqueSegregado");
        }
        if (filtro.getPossuiVp() != null) {
            conditions.add("e.possui_vp = :possuiVp");
        }
        if (filtro.getMunicipio() != null) {
            conditions.add("e.municipio_id = :municipio");
        }

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private String orderBy(EmpresaPaginaDTO filtro) {

        String field = filtro == null || filtro.getOrdenarPor() == null
                ? "descricao"
                : filtro.getOrdenarPor().toLowerCase(Locale.ROOT);
        String column = ORDER_BY_COLUMNS.get(field);
        if (column == null) {
            throw new IllegalArgumentException("Campo inválido para ordenação de empresas");
        }

        String direction = filtro == null || filtro.getDirecaoOrdenacao() == null
                ? "ASC"
                : filtro.getDirecaoOrdenacao().toUpperCase(Locale.ROOT);
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new IllegalArgumentException("A direção da ordenação deve ser ASC ou DESC");
        }

        return column + " " + direction + ", e.id ASC";
    }
}
