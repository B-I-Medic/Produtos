package com.medic.Web.repository.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoPorCDResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Repository
public class NecessidadeRepositoryCustomImpl implements NecessidadeRepositoryCustom {

    private static final String GROUP_BY_CD_AND_FILTER = """
                        select
                            centro_distribuicao,
                            cod_produto,
                            produto,
                            marca,
                            anvisa,
                            sum(estoque_interno) as estoque_interno,
                            sum(estoque_segregado) as estoque_segregado,
                            sum(estoque_vp) as estoque_vp,
                            sum(estoque_total) as estoque_total,
                            sum(demanda_orcado) as demanda_orcado,
                            sum(demanda_aprovado) as demanda_aprovado,
                            sum(demanda_agendado) as demanda_agendado,
                            sum(demanda_utilizado) as demanda_utilizado,
                            sum(demanda_total) as demanda_total,
                            sum(necessidade_de_compra) as necessidade_de_compra
                        from necessidade_de_compra
                        where (centro_distribuicao ilike :centro_distribuicao or :centro_distribuicao is null)
                            and (empresa ilike :empresa or :empresa is null)
                            and (municipio ilike :municipio or :municipio is null)
                            and (produto ilike :produto or :produto is null)
                            and (cod_produto ilike :produto or :produto is null)
                            and (marca ilike :marca or :marca is null)
                        group by centro_distribuicao, cod_produto, produto, marca, anvisa
                        order by necessidade_de_compra desc;
            """;

    private final DatabaseClient databaseClient;

    public NecessidadeRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<NecessidadeAgrupadoPorCDResponseDTO> findByCDFilter(NecessidadeFilterDTO filter) {

        var query = databaseClient.sql(GROUP_BY_CD_AND_FILTER);

        query = bindNullableText(query, "centro_distribuicao", filter.centroDistribuicao());
        query = bindNullableText(query, "empresa", filter.empresa());
        query = bindNullableText(query, "municipio", filter.municipio());
        query = bindNullableText(query, "produto", filter.produto());
        query = bindNullableText(query, "marca", filter.marca());

        return query.map((row, metadata) -> new NecessidadeAgrupadoPorCDResponseDTO(
                row.get("centro_distribuicao", String.class),
                row.get("cod_produto", String.class),
                row.get("produto", String.class),
                row.get("marca", String.class),
                row.get("anvisa", String.class),
                row.get("estoque_interno", Integer.class),
                row.get("estoque_segregado", Integer.class),
                row.get("estoque_vp", Integer.class),
                row.get("estoque_total", Integer.class),
                row.get("demanda_orcado", Integer.class),
                row.get("demanda_aprovado", Integer.class),
                row.get("demanda_agendado", Integer.class),
                row.get("demanda_utilizado", Integer.class),
                row.get("demanda_total", Integer.class),
                row.get("necessidade_de_compra", Integer.class)
        )).all();
    }

    private DatabaseClient.GenericExecuteSpec bindNullableText(DatabaseClient.GenericExecuteSpec query,
                                                               String name,
                                                               String value) {

        if (StringUtils.hasText(value)) {
            return query.bind(name, "%" + value.trim() + "%");
        }

        return query.bindNull(name, String.class);
    }
}
