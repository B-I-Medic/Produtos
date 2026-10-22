package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoPaginaDTO;
import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
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
public class EstoqueSegregadoRepositoryCustomImpl implements EstoqueSegregadoRepositoryCustom {

    private static final String FROM_SQL = """
            FROM estoque_segregado_catalogo esp
            JOIN empresa e ON e.id = esp.id_empresa
            LEFT JOIN municipio m ON m.cod_ibge = esp.cod_ibge
            """;

    private static final String FIND_PAGE_SQL = """
            SELECT esp.id AS id,
                   esp.cod_segregado AS cod_segregado,
                   esp.descricao AS descricao,
                   esp.ativo AS ativo,
                   e.id AS empresa_id,
                   e.descricao AS empresa_descricao,
                   e.viman AS empresa_viman,
                   e.codigo_empresa AS empresa_codigo_empresa,
                   m.id AS municipio_id,
                   m.descricao AS municipio_descricao,
                   m.cod_ibge AS municipio_codigo_ibge,
                   m.estado AS municipio_estado
            """ + FROM_SQL;

    private static final String COUNT_SQL = "SELECT COUNT(*) AS total " + FROM_SQL;

    private static final Map<String, String> ORDER_BY_COLUMNS = Map.of(
            "descricao", "LOWER(esp.descricao)",
            "municipio", "LOWER(m.descricao)"
    );

    private final DatabaseClient databaseClient;

    public EstoqueSegregadoRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<EstoqueSegregadoResponseDTO> findPageByEmpresaAndFilter(UUID idEmpresa,
                                                                         EstoqueSegregadoPaginaDTO filtro,
                                                                         long limite,
                                                                         long deslocamento) {
        String sql = FIND_PAGE_SQL + where(filtro) + " ORDER BY " + orderBy(filtro)
                + " LIMIT :limite OFFSET :deslocamento";

        var query = bindFilters(databaseClient.sql(sql), idEmpresa, filtro)
                .bind("limite", limite)
                .bind("deslocamento", deslocamento);

        return query.map((row, metadata) -> mapResponse(row)).all();
    }

    @Override
    public Mono<Long> countByEmpresaAndFilter(UUID idEmpresa, EstoqueSegregadoPaginaDTO filtro) {

        return bindFilters(databaseClient.sql(COUNT_SQL + where(filtro)), idEmpresa, filtro)
                .map((row, metadata) -> Objects.requireNonNull(row.get("total", Long.class)))
                .one();
    }

    private EstoqueSegregadoResponseDTO mapResponse(Row row) {

        var empresa = new EmpresaResponseDTO(
                row.get("empresa_id", UUID.class),
                row.get("empresa_descricao", String.class),
                null,
                Viman.valueOf(row.get("empresa_viman", String.class)),
                row.get("empresa_codigo_empresa", String.class),
                null, null, null
        );

        UUID municipioId = row.get("municipio_id", UUID.class);
        MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                municipioId,
                row.get("municipio_descricao", String.class),
                row.get("municipio_codigo_ibge", String.class),
                row.get("municipio_estado", String.class)
        );

        return new EstoqueSegregadoResponseDTO(
                row.get("id", UUID.class),
                row.get("cod_segregado", Integer.class),
                row.get("descricao", String.class),
                Boolean.TRUE.equals(row.get("ativo", Boolean.class)),
                empresa,
                municipio
        );
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec query,
                                                          UUID idEmpresa,
                                                          EstoqueSegregadoPaginaDTO filtro) {

        query = query.bind("idEmpresa", idEmpresa);

        if (filtro == null) return query;

        if (filtro.getDescricao() != null) query = query.bind("descricao", filtro.getDescricao());
        if (filtro.getMunicipio() != null) query = query.bind("municipio", filtro.getMunicipio());

        return query;
    }

    private String where(EstoqueSegregadoPaginaDTO filtro) {
        List<String> conditions = new ArrayList<>();

        conditions.add("esp.id_empresa = :idEmpresa");

        if (filtro != null && filtro.getDescricao() != null) {
            conditions.add("LOWER(TRIM(esp.descricao)) = LOWER(TRIM(:descricao))");
        }
        if (filtro != null && filtro.getMunicipio() != null) conditions.add("m.id = :municipio");

        return " WHERE " + String.join(" AND ", conditions);
    }

    private String orderBy(EstoqueSegregadoPaginaDTO filtro) {

        String field = filtro == null || filtro.getOrdenarPor() == null
                ? "descricao"
                : filtro.getOrdenarPor().toLowerCase(Locale.ROOT);

        String column = ORDER_BY_COLUMNS.get(field);

        if (column == null) throw new IllegalArgumentException("Campo inválido para ordenação de estoques segregados");

        String direction = filtro == null || filtro.getDirecaoOrdenacao() == null
                ? "ASC"
                : filtro.getDirecaoOrdenacao().toUpperCase(Locale.ROOT);

        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new IllegalArgumentException("A direção da ordenação deve ser ASC ou DESC");
        }

        return column + " " + direction + ", esp.id ASC";
    }
}
