package com.medic.web.repository.municipio;

import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioPaginaDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
public class MunicipioRepositoryCustomImpl implements MunicipioRepositoryCustom {

    private static final String FROM_SQL = """
            FROM municipio m
            LEFT JOIN centro_distribuicao cd ON cd.id = m.id_centro_distribuicao
            """;

    private static final String FIND_PAGE_SQL = """
            SELECT m.id AS id,
                   m.descricao AS descricao,
                   m.cod_ibge AS cod_ibge,
                   m.estado AS estado,
                   m.id_centro_distribuicao AS id_centro_distribuicao,
                   cd.id AS cd_id,
                   cd.descricao AS cd_descricao
            """ + FROM_SQL;

    private static final String COUNT_SQL = "SELECT COUNT(*) AS total " + FROM_SQL;

    private static final Map<String, String> ORDER_BY_COLUMNS = Map.of(
            "descricao", "LOWER(m.descricao)",
            "estado", "LOWER(m.estado)",
            "centrodistribuicao", "LOWER(cd.descricao)"
    );

    private final DatabaseClient databaseClient;

    public MunicipioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MunicipioResponseDTO> findPageByFilter(MunicipioPaginaDTO filtro, long limite, long deslocamento) {

        String sql = FIND_PAGE_SQL + where(filtro) + " ORDER BY " + orderBy(filtro)
                + " LIMIT :limite OFFSET :deslocamento";

        var query = bindFilters(databaseClient.sql(sql), filtro)
                .bind("limite", limite)
                .bind("deslocamento", deslocamento);

        return query.map((row, metadata) -> mapMunicipio(row)).all();
    }

    @Override
    public Mono<Long> countByFilter(MunicipioPaginaDTO filtro) {

        return bindFilters(databaseClient.sql(COUNT_SQL + where(filtro)), filtro)
                .map((row, metadata) -> Objects.requireNonNull(row.get("total", Long.class)))
                .one();
    }

    private MunicipioResponseDTO mapMunicipio(Row row) {

        UUID centroDistribuicaoId = row.get("cd_id", UUID.class);
        CentroDistribuicaoResponseDTO centroDistribuicao = centroDistribuicaoId == null
                ? null
                : new CentroDistribuicaoResponseDTO(centroDistribuicaoId, row.get("cd_descricao", String.class));

        return new MunicipioResponseDTO(
                row.get("id", UUID.class),
                row.get("descricao", String.class),
                row.get("cod_ibge", String.class),
                row.get("estado", String.class),
                centroDistribuicao
        );
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec query,
                                                           MunicipioPaginaDTO filtro) {

        if (filtro == null) return query;

        if (StringUtils.hasText(filtro.getDescricao())) query = query.bind("descricao", like(filtro.getDescricao()));
        if (StringUtils.hasText(filtro.getEstado())) query = query.bind("estado", like(filtro.getEstado()));
        if (filtro.getCentroDistribuicao() != null) query = query.bind("centroDistribuicao", filtro.getCentroDistribuicao());

        return query;
    }

    private String where(MunicipioPaginaDTO filtro) {

        if (filtro == null) return "";

        List<String> conditions = new ArrayList<>();

        if (StringUtils.hasText(filtro.getDescricao())) conditions.add("m.descricao ILIKE :descricao");
        if (StringUtils.hasText(filtro.getEstado())) conditions.add("m.estado ILIKE :estado");
        if (filtro.getCentroDistribuicao() != null) conditions.add("cd.id = :centroDistribuicao");

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private String like(String value) {
        return "%" + value.trim() + "%";
    }

    private String orderBy(MunicipioPaginaDTO filtro) {

        String field = filtro == null || filtro.getOrdenarPor() == null
                ? "descricao"
                : filtro.getOrdenarPor().toLowerCase(Locale.ROOT);

        String column = ORDER_BY_COLUMNS.get(field);

        if (column == null) throw new IllegalArgumentException("Campo inválido para ordenação de municípios");

        String direction = filtro == null || filtro.getDirecaoOrdenacao() == null
                ? "ASC"
                : filtro.getDirecaoOrdenacao().toUpperCase(Locale.ROOT);

        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new IllegalArgumentException("A direção da ordenação deve ser ASC ou DESC");
        }

        return column + " " + direction + ", m.id ASC";
    }
}
