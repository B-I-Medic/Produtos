package com.medic.Web.repository.config.estoque.vp;

import com.medic.Web.dto.config.estoque.vp.ValePermanenteFIlterDTO;
import com.medic.Web.dto.config.estoque.vp.ValePermanenteResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class ValePermanenteRepositoryCustomImpl implements ValePermanenteRepositoryCustom {

    private static final String FIND_ALL = """
            select vp.id as id,
                cd.descricao as cd,
                e.descricao as empresa,
                vp.cod_vp as cod_vp,
                m.descricao as municipio,
                m.estado as estado
            from vale_permanente_parametros vp
            join empresa_municipio em
                on em.id = vp.id_empresa_municipio
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
                and (m.estado ilike :estado or :estado is null)
                and (vp.cod_vp = :codVp or :codVp is null);
            """;

    private final DatabaseClient databaseClient;

    public ValePermanenteRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<ValePermanenteResponseDTO> getAllAndFilter(ValePermanenteFIlterDTO filter) {

        var query = databaseClient.sql(FIND_ALL);

        query = bindNullableText(query, "cd", filter == null ? null : filter.cd());
        query = bindNullableText(query, "empresa", filter == null ? null : filter.empresa());
        query = bindNullableText(query, "municipio", filter == null ? null : filter.municipio());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());
        query = bindNullableInteger(query, "codVp", filter == null ? null : filter.cod_vp());

        return query.map((row, metadata) -> new ValePermanenteResponseDTO(
                        row.get("id", UUID.class),
                        row.get("cd", String.class),
                        row.get("empresa", String.class),
                        row.get("cod_vp", Integer.class),
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

    private DatabaseClient.GenericExecuteSpec bindNullableInteger(DatabaseClient.GenericExecuteSpec query,
                                                                  String name,
                                                                  Integer value) {

        if (value != null) {
            return query.bind(name, value);
        }

        return query.bindNull(name, Integer.class);
    }
}
