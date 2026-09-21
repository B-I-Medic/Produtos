package com.medic.Web.service.auth;

import com.medic.Web.exception.type.auth.InvalidRefreshTokenException;
import com.medic.Web.model.auth.RefreshTokenModel;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.auth.RefreshTokenRepository;
import com.medic.Web.repository.usuario.UsuarioRepository;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceCoverageTest {

    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldIssueARefreshTokenForAnActiveUser() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        when(repository.save(any(RefreshTokenModel.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service().issue(user))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertNotNull(result.token());
                    org.junit.jupiter.api.Assertions.assertEquals(user.getId(), result.id() == null ? user.getId() : result.id());
                    org.junit.jupiter.api.Assertions.assertTrue(result.expiresAt().isAfter(Instant.now()));
                })
                .verifyComplete();
        verify(repository).save(any(RefreshTokenModel.class));
    }

    @Test
    void shouldRejectUnknownExpiredAndAlreadyConsumedTokens() {

        when(repository.findByTokenHash(anyString())).thenReturn(Mono.empty());
        StepVerifier.create(service().rotate("unknown"))
                .expectError(InvalidRefreshTokenException.class).verify();

        RefreshTokenModel expired = token(TestDataFactory.usuarioModel(), Instant.now().minus(1, ChronoUnit.MINUTES), null);
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(expired));
        StepVerifier.create(service().rotate("expired"))
                .expectError(InvalidRefreshTokenException.class).verify();
        verify(repository, never()).consumeIfActive(any(), any());

        RefreshTokenModel consumed = token(TestDataFactory.usuarioModel(), Instant.now().plus(1, ChronoUnit.DAYS), null);
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(consumed));
        when(repository.consumeIfActive(any(), any())).thenReturn(Mono.just(false));
        when(repository.revokeByFamilyId(any(), any())).thenReturn(Mono.just(1L));
        StepVerifier.create(service().rotate("consumed"))
                .expectError(InvalidRefreshTokenException.class).verify();
        verify(repository).revokeByFamilyId(eq(consumed.getFamilyId()), any());
    }

    @Test
    void shouldRejectMissingAndInactiveUsersDuringRotation() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        RefreshTokenModel token = token(user, Instant.now().plus(1, ChronoUnit.DAYS), null);
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(token));
        when(repository.consumeIfActive(any(), any())).thenReturn(Mono.just(true));
        when(usuarioRepository.findById(user.getId())).thenReturn(Mono.empty());

        StepVerifier.create(service().rotate("missing-user"))
                .expectError(InvalidRefreshTokenException.class).verify();

        UsuarioModel inactive = TestDataFactory.usuarioModel();
        inactive.setAtivo(false);
        RefreshTokenModel inactiveToken = token(inactive, Instant.now().plus(1, ChronoUnit.DAYS), null);
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(inactiveToken));
        when(usuarioRepository.findById(inactive.getId())).thenReturn(Mono.just(inactive));
        when(repository.revokeByFamilyId(any(), any())).thenReturn(Mono.just(1L));

        StepVerifier.create(service().rotate("inactive-user"))
                .expectError(DisabledException.class).verify();
        verify(repository).revokeByFamilyId(eq(inactiveToken.getFamilyId()), any());
    }

    @Test
    void shouldRevokeKnownTokensAndIgnoreAlreadyRevokedTokens() {

        UsuarioModel user = TestDataFactory.usuarioModel();
        RefreshTokenModel active = token(user, Instant.now().plus(1, ChronoUnit.DAYS), null);
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(active));
        when(repository.save(active)).thenReturn(Mono.just(active));

        StepVerifier.create(service().revoke("active"))
                .verifyComplete();
        verify(repository).save(active);

        RefreshTokenModel revoked = token(user, Instant.now().plus(1, ChronoUnit.DAYS), Instant.now());
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(revoked));
        StepVerifier.create(service().revoke("revoked"))
                .verifyComplete();

        verify(repository, never()).save(revoked);
    }

    @Test
    void shouldRevokeAllTokensForAUser() {

        UUID userId = UUID.randomUUID();
        when(repository.revokeByUsuarioId(eq(userId), any())).thenReturn(Mono.just(2L));

        StepVerifier.create(service().revokeAllForUser(userId))
                .verifyComplete();

        verify(repository).revokeByUsuarioId(eq(userId), any());
    }

    @Test
    void shouldUseTheDefaultValidityForMissingInvalidOrNonPositiveConfiguration() {

        assertEquals(30, RefreshTokenService.configuredValidityDays(null));
        assertEquals(30, RefreshTokenService.configuredValidityDays("  "));
        assertEquals(30, RefreshTokenService.configuredValidityDays("0"));
        assertEquals(30, RefreshTokenService.configuredValidityDays("-1"));
        assertEquals(30, RefreshTokenService.configuredValidityDays("not-a-number"));
        assertEquals(45, RefreshTokenService.configuredValidityDays("45"));
    }

    private RefreshTokenService service() {

        return new RefreshTokenService(repository, usuarioRepository);
    }

    private RefreshTokenModel token(UsuarioModel user, Instant expiresAt, Instant revokedAt) {

        return new RefreshTokenModel(
                UUID.randomUUID(),
                user.getId(),
                "hash",
                UUID.randomUUID(),
                expiresAt,
                Instant.now(),
                revokedAt,
                null,
                null
        );
    }
}
