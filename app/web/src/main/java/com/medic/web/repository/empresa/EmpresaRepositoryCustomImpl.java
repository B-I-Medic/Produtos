package com.medic.web.repository.empresa;

import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.model.empresa.Viman;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Repository
public class EmpresaRepositoryCustomImpl implements EmpresaRepositoryCustom {

    private static final String FIND_ALL_WITH_MUNICIPIO_SQL = """
            SELECT e.id AS empresa_id,
                   e.descricao AS empresa_descricao,
                   e.viman AS empresa_viman,
                   e.codigo_empresa AS empresa_codigo_empresa,
                   e.possui_estoque_interno AS empresa_possui_estoque_interno,
                   e.possui_estoque_segregado AS empresa_possui_estoque_segregado,
                   e.possui_vp AS empresa_possui_vp,
                   m.id AS municipio_id,
                   m.descricao AS municipio_descricao,
                   m.cod_ibge AS municipio_cod_ibge,
                   m.estado AS municipio_estado
              FROM empresa e
              LEFT JOIN municipio m
                ON m.id = e.municipio_id
            """;

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

    @Override
    public Flux<EmpresaResponseDTO> findAllWithMunicipio() {

        return databaseClient.sql(FIND_ALL_WITH_MUNICIPIO_SQL)
                .map((row, metadata) -> {

                    UUID municipioId = row.get("municipio_id", UUID.class);
                    String vimanValue = row.get("empresa_viman", String.class);

                    MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                            municipioId,
                            row.get("municipio_descricao", String.class),
                            row.get("municipio_cod_ibge", String.class),
                            row.get("municipio_estado", String.class)
                    );

                    return new EmpresaResponseDTO(
                            row.get("empresa_id", UUID.class),
                            row.get("empresa_descricao", String.class),
                            municipio,
                            vimanValue == null ? null : Viman.valueOf(vimanValue),
                            row.get("empresa_codigo_empresa", String.class),
                            Boolean.TRUE.equals(row.get("empresa_possui_estoque_interno", Boolean.class)),
                            Boolean.TRUE.equals(row.get("empresa_possui_estoque_segregado", Boolean.class)),
                            Boolean.TRUE.equals(row.get("empresa_possui_vp", Boolean.class))
                    );
                })
                .all();
    }
}
