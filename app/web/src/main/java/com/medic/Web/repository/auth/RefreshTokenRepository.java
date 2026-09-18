package com.medic.Web.repository.auth;

import com.medic.Web.model.auth.RefreshTokenModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshTokenModel, UUID>, RefreshTokenRepositoryCustom {

    Mono<RefreshTokenModel> findByTokenHash(String tokenHash);
}
