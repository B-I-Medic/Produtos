package com.medic.Web.repository.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class RefreshTokenRepositoryCustomImpl implements RefreshTokenRepositoryCustom {

    private final DatabaseClient databaseClient;

    public RefreshTokenRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> consumeIfActive(UUID tokenId, Instant agora) {
        return databaseClient.sql("""
                        UPDATE refresh_token
                           SET revogado_em = :agora,
                               ultimo_uso_em = :agora
                         WHERE id = :tokenId
                           AND revogado_em IS NULL
                           AND expira_em > :agora
                        """)
                .bind("agora", agora)
                .bind("tokenId", tokenId)
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0);
    }

    @Override
    public Mono<Long> revokeByFamilyId(UUID familyId, Instant agora) {
        return databaseClient.sql("""
                        UPDATE refresh_token
                           SET revogado_em = :agora
                         WHERE family_id = :familyId
                           AND revogado_em IS NULL
                        """)
                .bind("agora", agora)
                .bind("familyId", familyId)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> revokeByUsuarioId(UUID usuarioId, Instant agora) {
        return databaseClient.sql("""
                        UPDATE refresh_token
                           SET revogado_em = :agora
                         WHERE usuario_id = :usuarioId
                           AND revogado_em IS NULL
                        """)
                .bind("agora", agora)
                .bind("usuarioId", usuarioId)
                .fetch()
                .rowsUpdated();
    }
}
