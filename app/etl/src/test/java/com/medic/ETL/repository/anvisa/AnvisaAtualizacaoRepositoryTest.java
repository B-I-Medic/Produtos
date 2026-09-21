package com.medic.ETL.repository.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.model.anvisa.AnvisaAtualizacaoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnvisaAtualizacaoRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final AnvisaAtualizacaoRepository repository = new AnvisaAtualizacaoRepository(jdbcTemplate);

    @Test
    void shouldIgnoreTheInsertWhenTheBusinessDateAlreadyExists() {

        UUID id = UUID.randomUUID();
        UUID solicitadoPor = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 8, 21);
        Instant solicitadoEm = Instant.parse("2026-08-21T12:00:00Z");

        repository.insertIfAbsent(id, date, solicitadoPor, solicitadoEm);

        verify(jdbcTemplate).update(
                argThat(sql -> sql.contains("on conflict (data_referencia) do nothing")),
                eq(id),
                eq(date),
                eq("SOLICITADA"),
                eq(solicitadoPor),
                eq(Timestamp.from(solicitadoEm))
        );
    }

    @Test
    void shouldPreserveTheLastErrorWhenReopening() {

        UUID id = UUID.randomUUID();
        UUID solicitadoPor = UUID.randomUUID();
        Instant solicitadoEm = Instant.parse("2026-08-21T12:00:00Z");

        repository.reabrir(id, solicitadoPor, solicitadoEm);

        verify(jdbcTemplate).update(
                argThat(sql -> !sql.contains("ultimo_erro = null")),
                eq("SOLICITADA"),
                eq(solicitadoPor),
                eq(Timestamp.from(solicitadoEm)),
                eq(id)
        );
    }

    @Test
    void shouldMapAStoredRecordAndNullableDates() throws Exception {

        UUID id = UUID.randomUUID();
        UUID solicitadoPor = UUID.randomUUID();
        UUID processamentoId = UUID.randomUUID();
        UUID leaseToken = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 8, 21);
        Instant solicitadoEm = Instant.parse("2026-08-21T12:00:00Z");
        Instant leaseExpiraEm = Instant.parse("2026-08-21T13:00:00Z");
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getObject("id", UUID.class)).thenReturn(id);
        when(resultSet.getObject("data_referencia", LocalDate.class)).thenReturn(date);
        when(resultSet.getString("status")).thenReturn("EM_EXECUCAO");
        when(resultSet.getInt("tentativas")).thenReturn(2);
        when(resultSet.getObject("solicitado_por", UUID.class)).thenReturn(solicitadoPor);
        when(resultSet.getTimestamp("solicitado_em")).thenReturn(Timestamp.from(solicitadoEm));
        when(resultSet.getTimestamp("iniciado_em")).thenReturn(null);
        when(resultSet.getTimestamp("concluido_em")).thenReturn(null);
        when(resultSet.getTimestamp("ultima_falha_em")).thenReturn(null);
        when(resultSet.getString("ultimo_erro")).thenReturn("erro anterior");
        when(resultSet.getObject("processamento_id", UUID.class)).thenReturn(processamentoId);
        when(resultSet.getObject("lease_token", UUID.class)).thenReturn(leaseToken);
        when(resultSet.getTimestamp("lease_expira_em")).thenReturn(Timestamp.from(leaseExpiraEm));
        stubQuery(resultSet);

        Optional<AnvisaAtualizacaoRegistro> result = repository.findByDateForUpdate(date);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
        assertEquals(AnvisaAtualizacaoStatus.EM_EXECUCAO, result.get().status());
        assertEquals(solicitadoEm, result.get().solicitadoEm());
        assertEquals(leaseExpiraEm, result.get().leaseExpiraEm());
        assertEquals(null, result.get().iniciadoEm());
    }

    @Test
    void shouldFindNextAndSkipMarkingWhenThereIsNoInterruptedProcess() throws Exception {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id", UUID.class)).thenReturn(UUID.randomUUID());
        when(resultSet.getObject("data_referencia", LocalDate.class)).thenReturn(LocalDate.now());
        when(resultSet.getString("status")).thenReturn("SOLICITADA");
        when(resultSet.getInt("tentativas")).thenReturn(0);
        when(resultSet.getObject(anyString(), eq(UUID.class))).thenReturn(null);
        when(resultSet.getTimestamp(anyString())).thenReturn(null);
        when(resultSet.getString("ultimo_erro")).thenReturn(null);
        stubQuery(resultSet);

        Optional<AnvisaAtualizacaoRegistro> result = repository.findNextForClaim(
                Instant.parse("2026-08-21T12:00:00Z"), LocalDate.of(2026, 8, 21));
        repository.markInterruptedAttemptAsFailed(null, Instant.now());

        assertTrue(result.isPresent());
    }

    @Test
    void shouldExecuteRetryAndLifecycleUpdates() {

        UUID id = UUID.randomUUID();
        UUID processamentoId = UUID.randomUUID();
        UUID leaseToken = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-21T12:00:00Z");

        repository.markInterruptedAttemptAsFailed(processamentoId, now);
        repository.resetExpiredForRetry(id, now, "falha transitória");
        repository.failStale(id, now, "lease expirada");
        repository.start(id, processamentoId, leaseToken, now, now.plusSeconds(300));
        repository.complete(id, leaseToken, now.plusSeconds(400));
        repository.fail(id, leaseToken, now.plusSeconds(500), "erro final");

        verify(jdbcTemplate).update(argThat(sql -> sql.contains("update processamento")),
                eq(Timestamp.from(now)), eq(processamentoId));
        verify(jdbcTemplate).update(argThat(sql -> sql.contains("set status = ?")),
                eq("SOLICITADA"), eq(Timestamp.from(now)), eq("falha transitória"), eq(id));
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(ResultSet resultSet) throws Exception {

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    RowMapper<?> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });
    }
}
