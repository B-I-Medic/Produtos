package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Repository
public class AnvisaConfiguracaoRepository {

    private final DatabaseClient databaseClient;

    public AnvisaConfiguracaoRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<AnvisaConfiguracaoResponseDTO> find() {

        return databaseClient.sql("""
                        select retry_cooldown_minutos
                          from anvisa_configuracao
                         where id = 1
                        """)
                .map((row, metadata) -> map(row))
                .one();
    }

    public Mono<AnvisaConfiguracaoResponseDTO> update(int cooldownMinutos, UUID usuarioId) {

        return databaseClient.sql("""
                        update anvisa_configuracao
                           set retry_cooldown_minutos = :cooldown,
                               atualizado_por = :usuarioId,
                               atualizado_em = :atualizadoEm
                         where id = 1
                        """)
                .bind("cooldown", cooldownMinutos)
                .bind("usuarioId", usuarioId)
                .bind("atualizadoEm", Instant.now())
                .fetch()
                .rowsUpdated()
                .flatMap(rows -> rows == 0 ? Mono.empty() : find());
    }

    private AnvisaConfiguracaoResponseDTO map(Row row) {

        return new AnvisaConfiguracaoResponseDTO(
                row.get("retry_cooldown_minutos", Integer.class)
        );
    }
}
