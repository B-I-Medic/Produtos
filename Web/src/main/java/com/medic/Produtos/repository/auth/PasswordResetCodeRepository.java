package com.medic.Produtos.repository.auth;

import com.medic.Produtos.model.auth.PasswordResetCodeModel;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface PasswordResetCodeRepository extends ReactiveCrudRepository<PasswordResetCodeModel, UUID> {

    Mono<PasswordResetCodeModel> findFirstByEmailAndUsadoFalseAndExpiraEmAfterOrderByExpiraEmDesc(String email, Instant agora);
}
