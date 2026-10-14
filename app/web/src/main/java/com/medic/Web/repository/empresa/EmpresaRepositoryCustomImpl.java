package com.medic.Web.repository.empresa;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Repository
public class EmpresaRepositoryCustomImpl implements EmpresaRepositoryCustom {

    private final DatabaseClient databaseClient;

    public EmpresaRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> existsByIdEmpresaAndPossuiEstoqueSegregadoIsTrue(UUID idEmpresa) {

        var sql = """
                SELECT exists( select 1
                               FROM empresa
                               WHERE id = :idEmpresa AND possui_estoque_segregado = true)
                """;

        var query = databaseClient
                .sql(sql)
                .bind("idEmpresa", idEmpresa);

        return query.map((row, metaData) -> Objects.requireNonNull(row.get(0, Boolean.class))).one();
    }
}
