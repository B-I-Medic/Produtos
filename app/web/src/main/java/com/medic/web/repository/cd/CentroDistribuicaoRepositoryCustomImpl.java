package com.medic.web.repository.cd;

import com.medic.web.dto.cd.CentroDistribuicaoPaginaDTO;
import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
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
public class CentroDistribuicaoRepositoryCustomImpl implements CentroDistribuicaoRepositoryCustom {

    private static final String FIND_PAGE_SQL = """
            SELECT cd.id AS cd_id,
                   cd.descricao AS cd_descricao,
                   m.id AS municipio_id,
                   m.descricao AS municipio_descricao,
                   m.cod_ibge AS municipio_cod_ibge,
                   m.estado AS municipio_estado
              FROM centro_distribuicao cd
              LEFT JOIN municipio m
                ON m.id = cd.municipio_id
            """;

    private static final String COUNT_SQL = "SELECT COUNT(*) AS total FROM centro_distribuicao cd";

    private static final Map<String, String> ORDER_BY_COLUMNS = Map.of(
            "descricao", "LOWER(cd.descricao)",
            "municipio", "LOWER(m.descricao)"
    );

    private final DatabaseClient databaseClient;

    public CentroDistribuicaoRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<CentroDistribuicaoResponseDTO> findPageByFilter(CentroDistribuicaoPaginaDTO filtro, long limite, long deslocamento) {

        String sql = FIND_PAGE_SQL + where(filtro) + " ORDER BY " + orderBy(filtro)
                + " LIMIT :limite OFFSET :deslocamento";

        var query = bindFilters(databaseClient.sql(sql), filtro)
                .bind("limite", limite)
                .bind("deslocamento", deslocamento);

        return query.map((row, metadata) -> mapCentro(row)).all();
    }

    @Override
    public Mono<Long> countByFilter(CentroDistribuicaoPaginaDTO filtro) {

        return bindFilters(databaseClient.sql(COUNT_SQL + where(filtro)), filtro)
                .map((row, metadata) -> Objects.requireNonNull(row.get("total", Long.class)))
                .one();
    }

    private CentroDistribuicaoResponseDTO mapCentro(Row row) {

        UUID municipioId = row.get("municipio_id", UUID.class);

        MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                municipioId,
                row.get("municipio_descricao", String.class),
                row.get("municipio_cod_ibge", String.class),
                row.get("municipio_estado", String.class)
        );

        return new CentroDistribuicaoResponseDTO(
                row.get("cd_id", UUID.class),
                row.get("cd_descricao", String.class),
                municipio
        );
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec query,
                                                           CentroDistribuicaoPaginaDTO filtro) {
        if (filtro == null) return query;
        if (filtro.getDescricao() != null) query = query.bind("descricao", filtro.getDescricao());
        if (filtro.getMunicipio() != null) query = query.bind("municipio", filtro.getMunicipio());
        return query;
    }

    private String where(CentroDistribuicaoPaginaDTO filtro) {

        if (filtro == null) return "";

        List<String> conditions = new ArrayList<>();
        if (filtro.getDescricao() != null) conditions.add("LOWER(TRIM(cd.descricao)) = LOWER(TRIM(:descricao))");
        if (filtro.getMunicipio() != null) conditions.add("cd.municipio_id = :municipio");
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private String orderBy(CentroDistribuicaoPaginaDTO filtro) {
        String field = filtro == null || filtro.getOrdenarPor() == null
                ? "descricao" : filtro.getOrdenarPor().toLowerCase(Locale.ROOT);
        String column = ORDER_BY_COLUMNS.get(field);
        if (column == null) throw new IllegalArgumentException("Campo inválido para ordenação de centros de distribuição");
        String direction = filtro == null || filtro.getDirecaoOrdenacao() == null
                ? "ASC" : filtro.getDirecaoOrdenacao().toUpperCase(Locale.ROOT);
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new IllegalArgumentException("A direção da ordenação deve ser ASC ou DESC");
        }
        return column + " " + direction + ", cd.id ASC";
    }
}
