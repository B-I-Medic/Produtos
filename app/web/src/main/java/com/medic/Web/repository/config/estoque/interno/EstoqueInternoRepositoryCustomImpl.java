package com.medic.Web.repository.config.estoque.interno;

import com.medic.Web.dto.config.estoque.interno.EstoqueInternoFIlterDTO;
import com.medic.Web.dto.config.estoque.interno.EstoqueInternoResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class EstoqueInternoRepositoryCustomImpl implements EstoqueInternoRepositoryCustom {

    private static final String FIND_ALL = """
            select eip.id as id,
                cd.descricao as centro_distribuicao,
                e.descricao as empresa,
                m.descricao as municipio,
                m.estado as estado
            from estoque_interno_parametros eip
            join empresa_municipio em
                on em.id = eip.id_empresa_municipio
            join empresa e
                on e.id = em.id_empresa
            join municipio m
                on m.id = em.id_municipio
            join cd_empresa_municipio cem
                on cem.id_empresa_municipio = em.id
            join centro_distribuicao cd
                on cd.id = cem.id_cd
            where (cd.descricao ilike :cd or :cd is null)
                and (e.descricao ilike :empresa or :empresa is null)
                and (m.descricao ilike :municipio or :municipio is null)
                and (m.estado ilike :estado or :estado is null);
            """;

    private final DatabaseClient databaseClient;

    public EstoqueInternoRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<EstoqueInternoResponseDTO> getAllAndFilter(EstoqueInternoFIlterDTO filter) {

        var query = databaseClient.sql(FIND_ALL);

        query = bindNullableText(query, "cd", filter == null ? null : filter.cd());
        query = bindNullableText(query, "empresa", filter == null ? null : filter.empresa());
        query = bindNullableText(query, "municipio", filter == null ? null : filter.municipio());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());

        return query.map((row, metadata) -> new EstoqueInternoResponseDTO(
                    row.get("id", UUID.class),
                    row.get("centro_distribuicao", String.class),
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
