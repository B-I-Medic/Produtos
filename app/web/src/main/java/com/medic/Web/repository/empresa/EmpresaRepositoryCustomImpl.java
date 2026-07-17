package com.medic.Web.repository.empresa;

import com.medic.Web.dto.empresa.EmpresaMunicipioResponseDTO;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class EmpresaRepositoryCustomImpl implements EmpresaRepositoryCustom {

    private final DatabaseClient databaseClient;

    public EmpresaRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<EmpresaMunicipioResponseDTO> listEmpresaMunicipioByIdEmpresa(UUID idEmpresa) {

        var sql = """
                SELECT em.id AS id,
                    e.viman as viman,
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
                ORDER BY estado, municipio;
        """;

        var query = databaseClient
                .sql(sql)
                .bind("empresaId", idEmpresa);

        return query.map((row, metaData) -> new EmpresaMunicipioResponseDTO(
                row.get("id", UUID.class),
                row.get("viman", String.class),
                row.get("empresa", String.class),
                row.get("municipio", String.class),
                row.get("estado", String.class),
                row.get("centro_distribuicao", String.class)
        )).all();
    }
}
