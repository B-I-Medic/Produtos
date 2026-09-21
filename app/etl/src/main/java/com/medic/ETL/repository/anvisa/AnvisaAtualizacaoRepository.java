package com.medic.ETL.repository.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.model.anvisa.AnvisaAtualizacaoStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnvisaAtualizacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnvisaAtualizacaoRepository(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertIfAbsent(UUID id, LocalDate date, UUID solicitadoPor, Instant solicitadoEm) {

        jdbcTemplate.update("""
                insert into anvisa_atualizacao (
                    id, data_referencia, status, tentativas, solicitado_por, solicitado_em
                ) values (?, ?, ?, 0, ?, ?)
                on conflict (data_referencia) do nothing
                """, id, date, AnvisaAtualizacaoStatus.SOLICITADA.name(), solicitadoPor, timestamp(solicitadoEm));
    }

    public Optional<AnvisaAtualizacaoRegistro> findByDateForUpdate(LocalDate date) {

        return queryOne("""
                select id, data_referencia, status, tentativas, solicitado_por,
                       solicitado_em, iniciado_em, concluido_em, ultima_falha_em,
                       ultimo_erro, processamento_id, lease_token, lease_expira_em
                  from anvisa_atualizacao
                 where data_referencia = ?
                 for update
                """, date);
    }

    public void reabrir(UUID id, UUID solicitadoPor, Instant solicitadoEm) {
        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, solicitado_por = ?, solicitado_em = ?,
                       iniciado_em = null, concluido_em = null, processamento_id = null,
                       lease_token = null, lease_expira_em = null
                 where id = ?
                """, AnvisaAtualizacaoStatus.SOLICITADA.name(), solicitadoPor,
                timestamp(solicitadoEm), id);
    }

    public Optional<AnvisaAtualizacaoRegistro> findNextForClaim(Instant now, LocalDate currentDate) {

        return queryOne("""
                select id, data_referencia, status, tentativas, solicitado_por,
                       solicitado_em, iniciado_em, concluido_em, ultima_falha_em,
                       ultimo_erro, processamento_id, lease_token, lease_expira_em
                 from anvisa_atualizacao
                 where status = ?
                    or (status = ? and (lease_expira_em < ? or data_referencia < ?))
                 order by solicitado_em
                 limit 1
                 for update skip locked
                """, AnvisaAtualizacaoStatus.SOLICITADA.name(),
                AnvisaAtualizacaoStatus.EM_EXECUCAO.name(), timestamp(now), currentDate);
    }

    public void markInterruptedAttemptAsFailed(UUID processamentoId, Instant now) {

        if (processamentoId == null) {
            return;
        }

        jdbcTemplate.update("""
                update processamento
                   set status = 'FALHOU', concluido_em = ?
                 where id = ? and status = 'INICIADO'
                """, timestamp(now), processamentoId);
    }

    public void resetExpiredForRetry(UUID id, Instant falhaEm, String erro) {

        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, iniciado_em = null, processamento_id = null,
                       lease_token = null, lease_expira_em = null,
                       ultima_falha_em = ?, ultimo_erro = ?
                 where id = ?
                """, AnvisaAtualizacaoStatus.SOLICITADA.name(), timestamp(falhaEm), erro, id);
    }

    public void failStale(UUID id, Instant falhaEm, String erro) {
        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, ultima_falha_em = ?, ultimo_erro = ?,
                       lease_token = null, lease_expira_em = null
                 where id = ?
                """, AnvisaAtualizacaoStatus.FALHOU.name(), timestamp(falhaEm), erro, id);
    }

    public void start(UUID id, UUID processamentoId, UUID leaseToken, Instant iniciadoEm, Instant leaseExpiraEm) {

        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, tentativas = tentativas + 1, processamento_id = ?,
                       iniciado_em = ?, lease_token = ?, lease_expira_em = ?
                 where id = ?
                """, AnvisaAtualizacaoStatus.EM_EXECUCAO.name(), processamentoId,
                timestamp(iniciadoEm), leaseToken, timestamp(leaseExpiraEm), id);
    }

    public void complete(UUID id, UUID leaseToken, Instant concluidoEm) {
        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, concluido_em = ?, lease_token = null, lease_expira_em = null
                 where id = ? and lease_token = ?
                """, AnvisaAtualizacaoStatus.CONCLUIDA.name(), timestamp(concluidoEm), id, leaseToken);
    }

    public void fail(UUID id, UUID leaseToken, Instant falhaEm, String erro) {
        jdbcTemplate.update("""
                update anvisa_atualizacao
                   set status = ?, ultima_falha_em = ?, ultimo_erro = ?,
                       lease_token = null, lease_expira_em = null
                 where id = ? and lease_token = ?
                """, AnvisaAtualizacaoStatus.FALHOU.name(), timestamp(falhaEm), erro, id, leaseToken);
    }

    private Optional<AnvisaAtualizacaoRegistro> queryOne(String sql, Object... parameters) {

        List<AnvisaAtualizacaoRegistro> records = jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new AnvisaAtualizacaoRegistro(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getObject("data_referencia", LocalDate.class),
                        AnvisaAtualizacaoStatus.valueOf(resultSet.getString("status")),
                        resultSet.getInt("tentativas"),
                        resultSet.getObject("solicitado_por", UUID.class),
                        instant(resultSet.getTimestamp("solicitado_em")),
                        instant(resultSet.getTimestamp("iniciado_em")),
                        instant(resultSet.getTimestamp("concluido_em")),
                        instant(resultSet.getTimestamp("ultima_falha_em")),
                        resultSet.getString("ultimo_erro"),
                        resultSet.getObject("processamento_id", UUID.class),
                        resultSet.getObject("lease_token", UUID.class),
                        instant(resultSet.getTimestamp("lease_expira_em"))
                ), parameters);

        return records.stream().findFirst();
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
