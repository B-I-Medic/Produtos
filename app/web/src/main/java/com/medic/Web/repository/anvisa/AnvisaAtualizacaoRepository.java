package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public class AnvisaAtualizacaoRepository {

    private final DatabaseClient databaseClient;

    public AnvisaAtualizacaoRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<AnvisaAtualizacaoResponseDTO> findById(UUID id) {

        return databaseClient.sql(selectBase() + " where a.id = :id")
                .bind("id", id)
                .map((row, metadata) -> map(row))
                .one();
    }

    public Mono<AnvisaAtualizacaoResponseDTO> findToday(LocalDate date) {

        return databaseClient.sql(selectBase() + " where a.data_referencia = :dataReferencia")
                .bind("dataReferencia", date)
                .map((row, metadata) -> map(row))
                .one();
    }

    private String selectBase() {
        return """
                select a.id, a.data_referencia, a.status, a.tentativas,
                       a.solicitado_em, a.iniciado_em, a.concluido_em,
                       a.ultima_falha_em, a.processamento_id, a.ultimo_erro,
                       case when a.status = 'FALHOU'
                                  and a.data_referencia =
                                      (current_timestamp at time zone 'America/Sao_Paulo')::date
                            then a.ultima_falha_em + (c.retry_cooldown_minutos * interval '1 minute')
                            else null end as proxima_solicitacao_em
                  from anvisa_atualizacao a
                  cross join anvisa_configuracao c
                """;
    }

    private AnvisaAtualizacaoResponseDTO map(Row row) {

        return new AnvisaAtualizacaoResponseDTO(
                row.get("id", UUID.class),
                row.get("data_referencia", LocalDate.class),
                row.get("status", String.class),
                row.get("tentativas", Integer.class) == null ? 0 : row.get("tentativas", Integer.class),
                row.get("solicitado_em", Instant.class),
                row.get("iniciado_em", Instant.class),
                row.get("concluido_em", Instant.class),
                row.get("ultima_falha_em", Instant.class),
                row.get("proxima_solicitacao_em", Instant.class),
                row.get("processamento_id", UUID.class),
                row.get("ultimo_erro", String.class)
        );
    }
}
