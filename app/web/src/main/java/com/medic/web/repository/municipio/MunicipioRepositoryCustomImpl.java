package com.medic.web.repository.municipio;

import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioFilterDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
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
                       m.estado AS estado,
                       m.id_centro_distribuicao AS id_centro_distribuicao,
                       cd.id AS cd_id,
                       cd.descricao AS cd_descricao
                  FROM municipio m
                  LEFT JOIN centro_distribuicao cd
                    ON cd.id = m.id_centro_distribuicao
                 WHERE (:descricao IS NULL OR m.descricao ILIKE :descricao)
                   AND (:estado IS NULL OR m.estado ILIKE :estado)
                   AND (:centroDistribuicao IS NULL OR cd.descricao ILIKE :centroDistribuicao)
                """;

    private final DatabaseClient databaseClient;

    public MunicipioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MunicipioResponseDTO> findByFiltro(MunicipioFilterDTO filter) {

        var query = databaseClient.sql(FIND_BY_FILTRO_SQL);
        query = bindNullableText(query, "descricao", filter == null ? null : filter.descricao());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());
        query = bindNullableText(query, "centroDistribuicao", filter == null ? null : filter.centroDistribuicao());

        return query.map((row, metadata) -> {

                    UUID centroDistribuicaoId = row.get("cd_id", UUID.class);

                    CentroDistribuicaoResponseDTO centroDistribuicao = centroDistribuicaoId == null ? null : new CentroDistribuicaoResponseDTO(
                            centroDistribuicaoId,
                            row.get("cd_descricao", String.class)
                    );

                    return new MunicipioResponseDTO(
                            row.get("id", UUID.class),
                            row.get("descricao", String.class),
                            row.get("cod_ibge", String.class),
                            row.get("estado", String.class),
                            centroDistribuicao
                    );
                })
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
