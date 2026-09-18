package com.medic.Web.repository.auth;

import java.time.Instant;
import java.util.UUID;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepositoryCustom {

    Mono<Boolean> consumeIfActive(UUID tokenId, Instant agora);

    Mono<Long> revokeByFamilyId(UUID familyId, Instant agora);

    Mono<Long> revokeByUsuarioId(UUID usuarioId, Instant agora);
}
