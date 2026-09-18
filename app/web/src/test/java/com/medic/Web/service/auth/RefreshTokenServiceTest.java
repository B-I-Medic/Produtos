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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldRotateActiveToken() {
        UsuarioModel usuario = TestDataFactory.usuarioModel();
        RefreshTokenModel token = token(usuario, null);

        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(token));
        when(repository.consumeIfActive(any(), any())).thenReturn(Mono.just(true));
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Mono.just(usuario));
        when(repository.save(any(RefreshTokenModel.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        RefreshTokenService service = new RefreshTokenService(repository, usuarioRepository);

        StepVerifier.create(service.rotate("refresh"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(usuario, result.usuario());
                    org.junit.jupiter.api.Assertions.assertNotEquals("refresh", result.token());
                    org.junit.jupiter.api.Assertions.assertTrue(result.expiresAt().isAfter(Instant.now()));
                })
                .verifyComplete();

        verify(repository).consumeIfActive(any(), any());
    }

    @Test
    void shouldRevokeFamilyWhenRevokedTokenIsReused() {
        UsuarioModel usuario = TestDataFactory.usuarioModel();
        UUID familyId = UUID.randomUUID();
        RefreshTokenModel token = token(usuario, Instant.now());
        token.setFamilyId(familyId);

        when(repository.findByTokenHash(anyString())).thenReturn(Mono.just(token));
        when(repository.revokeByFamilyId(eq(familyId), any())).thenReturn(Mono.just(1L));

        RefreshTokenService service = new RefreshTokenService(repository, usuarioRepository);

        StepVerifier.create(service.rotate("refresh"))
                .expectError(InvalidRefreshTokenException.class)
                .verify();

        verify(repository).revokeByFamilyId(eq(familyId), any());
    }

    @Test
    void shouldLogoutWithoutFailingForUnknownToken() {
        when(repository.findByTokenHash(anyString())).thenReturn(Mono.empty());

        RefreshTokenService service = new RefreshTokenService(repository, usuarioRepository);

        StepVerifier.create(service.revoke("refresh"))
                .verifyComplete();
    }

    private RefreshTokenModel token(UsuarioModel usuario, Instant revogadoEm) {
        return new RefreshTokenModel(
                UUID.randomUUID(),
                usuario.getId(),
                "hash",
                UUID.randomUUID(),
                Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS),
                Instant.now(),
                revogadoEm,
                null,
                null
        );
    }
}
