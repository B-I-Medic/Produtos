package com.medic.Web.repository.cd;

import com.medic.Web.dto.municipio.MunicipioFilterDTO;
import com.medic.Web.model.municipio.MunicipioModel;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class MunicipioRepositoryCustomImpl implements MunicipioRepositoryCustom {

    private static final String FIND_BY_FILTRO_SQL = """
                SELECT m.id AS id,
                       m.descricao AS descricao,
                       m.cod_ibge AS cod_ibge,
                       m.estado AS estado
                  FROM municipio m
                 WHERE (:descricao IS NULL OR m.descricao ILIKE :descricao)
                   AND (:estado IS NULL OR m.estado ILIKE :estado)
                """;

    private final DatabaseClient databaseClient;

    public MunicipioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MunicipioModel> findByFiltro(MunicipioFilterDTO filter) {

        var query = databaseClient.sql(FIND_BY_FILTRO_SQL);
        query = bindNullableText(query, "descricao", filter == null ? null : filter.descricao());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());

        return query.map((row, metadata) -> new MunicipioModel(
                        row.get("id", UUID.class),
                        row.get("descricao", String.class),
                        row.get("cod_ibge", String.class),
                        row.get("estado", String.class)
                ))
                .all();
    }

    private String like(String value) {

        return "%" + value.trim() + "%";
    }

    private DatabaseClient.GenericExecuteSpec bindNullableText(DatabaseClient.GenericExecuteSpec query,
                                                               String name,
                                                               String value) {

        if (StringUtils.hasText(value)) {
            return query.bind(name, like(value));
        }

        return query.bindNull(name, String.class);
    }
}
