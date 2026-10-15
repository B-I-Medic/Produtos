package com.medic.web.repository.cd;

import com.medic.web.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class CentroDistribuicaoRepositoryCustomImpl implements CentroDistribuicaoRepositoryCustom {

    private static final String FIND_ALL_WITH_MUNICIPIO_SQL = """
            SELECT cd.id AS cd_id,
                   cd.descricao AS cd_descricao,
                   m.id AS municipio_id,
                   m.descricao AS municipio_descricao,
                   m.cod_ibge AS municipio_cod_ibge,
                   m.estado AS municipio_estado
              FROM centro_distribuicao cd
              LEFT JOIN municipio m
                ON m.id = cd.municipio_id
            """;

    private final DatabaseClient databaseClient;

    public CentroDistribuicaoRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<CentroDistribuicaoResponseDTO> findAllWithMunicipio() {

        return databaseClient.sql(FIND_ALL_WITH_MUNICIPIO_SQL)
                .map((row, metadata) -> {

                    UUID municipioId = row.get("municipio_id", UUID.class);

                    MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                            municipioId,
                            row.get("municipio_descricao", String.class),
                            row.get("municipio_cod_ibge", String.class),
                            row.get("municipio_estado", String.class)
                    );

                    return new CentroDistribuicaoResponseDTO(
                            row.get("cd_id", UUID.class),
                            row.get("cd_descricao", String.class),
                            municipio
                    );
                })
                .all();
    }
}
