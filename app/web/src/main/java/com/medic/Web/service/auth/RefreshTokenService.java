package com.medic.Web.service.auth;

import com.medic.Web.exception.type.auth.InvalidRefreshTokenException;
import com.medic.Web.model.auth.RefreshTokenModel;
import com.medic.Web.model.usuario.UsuarioModel;
import com.medic.Web.repository.auth.RefreshTokenRepository;
import com.medic.Web.repository.usuario.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final long DEFAULT_REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final SecureRandom secureRandom = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final long refreshTokenValidityDays;

    public RefreshTokenService(RefreshTokenRepository repository,
                               UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenValidityDays = configuredValidityDays();
    }

    public Mono<IssuedRefreshToken> issue(UsuarioModel usuario) {
        return issue(usuario, UUID.randomUUID());
    }

    public Mono<RotatedRefreshToken> rotate(String rawToken) {
        Instant agora = Instant.now();

        return repository.findByTokenHash(hash(rawToken))
                .switchIfEmpty(Mono.error(new InvalidRefreshTokenException()))
                .flatMap(token -> {
                    if (token.isRevogado()) {
                        return repository.revokeByFamilyId(token.getFamilyId(), agora)
                                .then(Mono.error(new InvalidRefreshTokenException()));
                    }

                    if (token.getExpiraEm() == null || !token.getExpiraEm().isAfter(agora)) {
                        return Mono.error(new InvalidRefreshTokenException());
                    }

                    return repository.consumeIfActive(token.getId(), agora)
                            .flatMap(consumido -> {
                                if (!consumido) {
                                    return repository.revokeByFamilyId(token.getFamilyId(), agora)
                                            .then(Mono.error(new InvalidRefreshTokenException()));
                                }

                                return usuarioRepository.findById(token.getUsuarioId())
                                        .switchIfEmpty(Mono.error(new InvalidRefreshTokenException()))
                                        .flatMap(usuario -> {
                                            if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                                                return repository.revokeByFamilyId(token.getFamilyId(), agora)
                                                        .then(Mono.error(new DisabledException("Usuario inativo")));
                                            }

                                            return issue(usuario, token.getFamilyId())
                                                    .flatMap(next -> {
                                                        token.setSubstituidoPor(next.id());
                                                        return repository.save(token)
                                                                .thenReturn(new RotatedRefreshToken(usuario, next.token(), next.expiresAt()));
                                                    });
                                        });
                            });
                });
    }

    public Mono<Void> revoke(String rawToken) {
        return repository.findByTokenHash(hash(rawToken))
                .flatMap(token -> {
                    if (token.isRevogado()) {
                        return Mono.empty();
                    }

                    token.setRevogadoEm(Instant.now());
                    return repository.save(token).then();
                })
                .then();
    }

    public Mono<Void> revokeAllForUser(UUID usuarioId) {
        return repository.revokeByUsuarioId(usuarioId, Instant.now()).then();
    }

    private Mono<IssuedRefreshToken> issue(UsuarioModel usuario, UUID familyId) {
        String rawToken = generateRawToken();
        Instant criadoEm = Instant.now();
        Instant expiraEm = criadoEm.plus(refreshTokenValidityDays, ChronoUnit.DAYS);

        RefreshTokenModel token = new RefreshTokenModel(
                null,
                usuario.getId(),
                hash(rawToken),
                familyId,
                expiraEm,
                criadoEm,
                null,
                null,
                null
        );

        return repository.save(token)
                .map(saved -> new IssuedRefreshToken(saved.getId(), rawToken, saved.getExpiraEm()));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private long configuredValidityDays() {
        return configuredValidityDays(System.getenv("JWT_REFRESH_TOKEN_DAYS"));
    }

    static long configuredValidityDays(String configured) {

        if (configured == null || configured.isBlank()) {
            return DEFAULT_REFRESH_TOKEN_VALIDITY_DAYS;
        }

        try {
            long days = Long.parseLong(configured);
            return days > 0 ? days : DEFAULT_REFRESH_TOKEN_VALIDITY_DAYS;
        } catch (NumberFormatException ex) {
            return DEFAULT_REFRESH_TOKEN_VALIDITY_DAYS;
        }
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponivel", ex);
        }
    }

    public record IssuedRefreshToken(UUID id, String token, Instant expiresAt) {
    }

    public record RotatedRefreshToken(UsuarioModel usuario, String token, Instant expiresAt) {
    }
}
