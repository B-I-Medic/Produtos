package com.medic.Web.repository.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.model.processamento.ProcessamentoDisparo;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.model.processamento.ProcessamentoStatus;
import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.UUID;

@Repository
public class ProcessamentoRepository {

    private final DatabaseClient databaseClient;

    public ProcessamentoRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<ProcessamentoResponseDTO> findUltimosConcluidos() {

        return databaseClient.sql("""
                        select distinct on (p.entidade)
                               p.id,
                               p.entidade,
                               p.status,
                               p.tipo_disparo,
                               p.iniciado_em,
                               p.concluido_em
                          from processamento p
                         where p.status = 'CONCLUIDO'
                           and p.concluido_em is not null
                           and p.entidade in ('ESTOQUE', 'PRODUTOS', 'DEMANDA', 'ANVISA')
                         order by p.entidade,
                                  p.concluido_em desc,
                                  p.iniciado_em desc,
                                  p.id desc
                        """)
                .map((row, metadata) -> map(row))
                .all();
    }

    private ProcessamentoResponseDTO map(Row row) {

        return new ProcessamentoResponseDTO(
                ProcessamentoEntidade.valueOf(row.get("entidade", String.class)),
                row.get("id", UUID.class),
                ProcessamentoStatus.valueOf(row.get("status", String.class)),
                ProcessamentoDisparo.valueOf(row.get("tipo_disparo", String.class)),
                row.get("iniciado_em", Instant.class),
                row.get("concluido_em", Instant.class)
        );
    }
}
