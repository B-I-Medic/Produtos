package com.medic.web.repository.config.estoque.segregado;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;
import com.medic.web.model.empresa.Viman;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public class EstoqueSegregadoRepositoryCustomImpl implements EstoqueSegregadoRepositoryCustom {

    private static final String FIND_ALL = """
            select esp.id as id,
                esp.cod_segregado as cod_segregado,
                esp.descricao as descricao,
                esp.ativo as ativo,
                e.id as empresa_id,
                e.descricao as empresa_descricao,
                e.viman as empresa_viman,
                e.codigo_empresa as empresa_codigo_empresa,
                m.id as municipio_id,
                m.descricao as municipio_descricao,
                m.cod_ibge as municipio_codigo_ibge,
                m.estado as municipio_estado
            from estoque_segregado_catalogo esp
            join empresa e
                on e.id = esp.id_empresa
            left join municipio m
                on m.cod_ibge = esp.cod_ibge
            where esp.id_empresa = :idEmpresa;
            """;

    private final DatabaseClient databaseClient;

    public EstoqueSegregadoRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<EstoqueSegregadoResponseDTO> getAllByIdEmpresa(UUID idEmpresa) {

        var query = databaseClient.sql(FIND_ALL).bind("idEmpresa", idEmpresa);

        return query.map((row, metadata) -> {

                    var empresa = new EmpresaResponseDTO(
                            row.get("empresa_id", UUID.class),
                            row.get("empresa_descricao", String.class),
                            null,
                            Viman.valueOf(row.get("empresa_viman", String.class)),
                            row.get("empresa_codigo_empresa", String.class),
                            null,
                            null,
                            null
                    );

                    UUID municipioId = row.get("municipio_id", UUID.class);
                    MunicipioResponseDTO municipio = municipioId == null ? null : new MunicipioResponseDTO(
                            municipioId,
                            row.get("municipio_descricao", String.class),
                            row.get("municipio_codigo_ibge", String.class),
                            row.get("municipio_estado", String.class)
                    );

                    return new EstoqueSegregadoResponseDTO(
                            row.get("id", UUID.class),
                            row.get("cod_segregado", Integer.class),
                            row.get("descricao", String.class),
                            Boolean.TRUE.equals(row.get("ativo", Boolean.class)),
                            empresa,
                            municipio
                    );
                })
                .all();
    }

}
