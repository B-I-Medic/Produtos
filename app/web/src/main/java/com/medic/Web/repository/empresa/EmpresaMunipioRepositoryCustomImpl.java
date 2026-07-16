package com.medic.Web.repository.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioFilterDTO;
import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class EmpresaMunipioRepositoryCustomImpl implements EmpresaMunipioRepositoryCustom {

    private static final String FIND_BY_FILTRO_SQL = """
                        SELECT em.id AS id,
                            e.descricao AS empresa,
                            m.descricao AS municipio,
                            m.estado AS estado,
                            cd.descricao AS centro_distribuicao
                        FROM empresa_municipio em
                        JOIN empresa e ON e.id = em.id_empresa
                        JOIN municipio m ON m.id = em.id_municipio
                        JOIN cd_empresa_municipio cem ON cem.id_empresa_municipio = em.id
                        JOIN centro_distribuicao cd ON cd.id = cem.id_cd
                        WHERE e.id = :empresaId
                          AND (:municipio IS NULL OR m.descricao ILIKE :municipio)
                          AND (:estado IS NULL OR m.estado ILIKE :estado)
            """;

    private final DatabaseClient databaseClient;

    public EmpresaMunipioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<EmpresaMunicipioResponseDTO> getAllAndFilter(UUID empresaId,
                                                             EmpresaMunicipioFilterDTO filter) {
        var query = databaseClient.sql(FIND_BY_FILTRO_SQL)
                .bind("empresaId", empresaId);
        query = bindNullableText(query, "municipio", filter == null ? null : filter.municipio());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());

        return query.map((row, metadata) -> new EmpresaMunicipioResponseDTO(
                        row.get("id", UUID.class),
                        row.get("empresa", String.class),
                        row.get("municipio", String.class),
                        row.get("estado", String.class),
                        row.get("centro_distribuicao", String.class)
                ))
                .all();
    }

    @Override
    public Mono<EmpresaMunicipioResponseDTO> findByIdCustom(UUID id) {

        var sql = """
                SELECT em.id AS id,
                    e.descricao AS empresa,
                    m.descricao AS municipio,
                    m.estado AS estado,
                    cd.descricao AS centro_distribuicao
                FROM empresa_municipio em
                JOIN empresa e ON e.id = em.id_empresa
                JOIN municipio m ON m.id = em.id_municipio
                JOIN cd_empresa_municipio cem ON cem.id_empresa_municipio = em.id
                JOIN centro_distribuicao cd ON cd.id = cem.id_cd
                WHERE em.id = :empresaId
        """;

        var query = databaseClient
                .sql(sql)
                .bind("empresaId", id);

        return query.map((row, metaData) -> new EmpresaMunicipioResponseDTO(
                row.get("id", UUID.class),
                row.get("empresa", String.class),
                row.get("municipio", String.class),
                row.get("estado", String.class),
                row.get("centro_distribuicao", String.class)
                )).one();
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
