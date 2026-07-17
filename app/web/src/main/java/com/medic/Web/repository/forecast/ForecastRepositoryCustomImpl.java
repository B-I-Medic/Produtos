package com.medic.Web.repository.forecast;

import com.medic.Web.dto.forecast.ForecastAgrupadoResponseDTO;
import com.medic.Web.dto.forecast.ForecastFilterDTO;
import com.medic.Web.dto.forecast.AgrupamentosPadrao;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Repository
public class ForecastRepositoryCustomImpl implements ForecastRepositoryCustom {

    private static final List<AgrupamentosPadrao> DEFAULT_GROUP_BY = List.of(AgrupamentosPadrao.values());

    private static final String BASE_SQL = """
                        select
                            %s,
                            sum(estoque_interno) as estoque_interno,
                            sum(estoque_segregado) as estoque_segregado,
                            sum(estoque_vp) as estoque_vp,
                            sum(estoque_total) as estoque_total,
                            sum(demanda_orcado) as demanda_orcado,
                            sum(demanda_aprovado) as demanda_aprovado,
                            sum(demanda_agendado) as demanda_agendado,
                            sum(demanda_utilizado) as demanda_utilizado,
                            sum(demanda_total) as demanda_total,
                            case
                                when sum(necessidade_de_compra) >= 0 then 0
                                else (sum(necessidade_de_compra) * -1)
                            end as necessidade_de_compra_real
                        from necessidade_de_compra
                        where (centro_distribuicao ilike :centro_distribuicao or :centro_distribuicao is null)
                            and (empresa ilike :empresa or :empresa is null)
                            and (municipio ilike :municipio or :municipio is null)
                            and (
                                (produto ilike :produto or :produto is null)
                                or (cod_produto ilike :produto or :produto is null)
                               )
                            and (marca ilike :marca or :marca is null)
                            and (anvisa like :anvisa or :anvisa is null)
                            and (estado like :estado or :estado is null)
                        group by %s
                        order by
                            centro_distribuicao asc,
                            empresa asc,
                            necessidade_de_compra_real desc;
            """;

    private final DatabaseClient databaseClient;

    public ForecastRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<ForecastAgrupadoResponseDTO> findByFilter(ForecastFilterDTO filter) {

        var groupByColumns = resolveGroupByColumns(filter == null ? null : filter.groupBy());
        var query = databaseClient.sql(buildSql(groupByColumns));

        query = bindNullableText(query, "centro_distribuicao", filter == null ? null : filter.centroDistribuicao());
        query = bindNullableText(query, "empresa", filter == null ? null : filter.empresa());
        query = bindNullableText(query, "estado", filter == null ? null : filter.estado());
        query = bindNullableText(query, "municipio", filter == null ? null : filter.municipio());
        query = bindNullableText(query, "anvisa", filter == null ? null : filter.anvisa());
        query = bindNullableText(query, "marca", filter == null ? null : filter.marca());
        query = bindNullableText(query, "produto", filter == null ? null : filter.produto());

        var selectedColumns = Set.copyOf(groupByColumns);

        return query.map((row, metadata) -> new ForecastAgrupadoResponseDTO(
                        readString(row, "centro_distribuicao", selectedColumns),
                        readString(row, "empresa", selectedColumns),
                        readString(row, "estado", selectedColumns),
                        readString(row, "municipio", selectedColumns),
                        readString(row, "anvisa", selectedColumns),
                        readString(row, "marca", selectedColumns),
                        readString(row, "cod_produto", selectedColumns),
                        readString(row, "produto", selectedColumns),
                        row.get("estoque_interno", Long.class),
                        row.get("estoque_segregado", Long.class),
                        row.get("estoque_vp", Long.class),
                        row.get("estoque_total", Long.class),
                        row.get("demanda_orcado", Long.class),
                        row.get("demanda_aprovado", Long.class),
                        row.get("demanda_agendado", Long.class),
                        row.get("demanda_utilizado", Long.class),
                        row.get("demanda_total", Long.class),
                        row.get("necessidade_de_compra_real", Long.class)
                ))
                .all();
    }

    private String buildSql(List<String> groupByColumns) {

        var selectColumns = String.join(",", groupByColumns);
        var groupBy = String.join(", ", groupByColumns);

        return BASE_SQL.formatted(selectColumns, groupBy);
    }

    private List<String> resolveGroupByColumns(List<AgrupamentosPadrao> groupBy) {

        if (groupBy == null || groupBy.isEmpty()) {
            return DEFAULT_GROUP_BY.stream().map(AgrupamentosPadrao::getDescricao).toList();
        }

        return groupBy.stream().map(AgrupamentosPadrao::getDescricao).toList();
    }

    private String readString(Row row, String name, Set<String> selectedColumns) {

        if (selectedColumns.contains(name)) {
            return row.get(name, String.class);
        }

        return null;
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
