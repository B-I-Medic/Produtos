package com.medic.Web.repository.cd;

import com.medic.Web.dto.cd.CdEmpresaMunicipioFilterDTO;
import com.medic.Web.dto.cd.CdEmpresaMunicipioResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class CdEmpresaMunipioRepositoryCustomImpl implements CdEmpresaMunipioRepositoryCustom {

    private static final String FIND_BY_FILTRO_SQL = """
                SELECT em.id AS id,
                       e.descricao AS empresa,
                       m.descricao AS municipio,
                       m.estado AS estado
                  FROM cd_empresa_municipio cdm
                  JOIN empresa_municipio em ON em.id = cdm.id_empresa_municipio
                  JOIN empresa e ON e.id = em.id_empresa
                  JOIN municipio m ON m.id = em.id_municipio
                 WHERE cdm.id_cd = :cdId
                   AND (:empresa IS NULL OR e.descricao ILIKE :empresa)
                   AND (:municipio IS NULL OR m.descricao ILIKE :municipio)
                   AND (:estado IS NULL OR m.estado ILIKE :estado)
                """;

    private final DatabaseClient databaseClient;

    public CdEmpresaMunipioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<CdEmpresaMunicipioResponseDTO> findByFiltro(UUID cdId,
                                                            CdEmpresaMunicipioFilterDTO filter) {

        var query = databaseClient.sql(FIND_BY_FILTRO_SQL)
                .bind("cdId", cdId);
        query = bindNullableText(query, "empresa", filter == null ? null : filter.empresa());
        query = bindNullableText(query, "municipio", filter == null ? null : filter.municipio());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());

        return query.map((row, metadata) -> new CdEmpresaMunicipioResponseDTO(
                        row.get("id", UUID.class),
                        row.get("empresa", String.class),
                        row.get("municipio", String.class),
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
