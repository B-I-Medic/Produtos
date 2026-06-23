package com.medic.Produtos.service.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medic.Produtos.model.usuario.UsuarioModel;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final Algorithm algorithm;

    public JwtService() {
        this.algorithm = Algorithm.HMAC256(
            System.getenv("JWT_SECRET")
        );
    }

    public boolean isValid(String token) {

        try {
            return verify(token).getSubject() != null;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }

    public String getSubject(String token) {

        return verify(token).getSubject();
    }

    public List<String> getRoles(String token) {

        String role = verify(token)
                .getClaim("roles")
                .asString();

        if (role == null) {
            return List.of();
        }

        return List.of(role);
    }

    public String generateToken(UsuarioModel user) {

        return JWT.create()
                .withIssuer("produto-auth-api")
                .withSubject(user.getEmail())
                .withClaim("roles", user.getRole().name())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    public Instant getExpires_in(String token) {

        return JWT.decode(token)
                .getExpiresAtAsInstant();
    }

    private DecodedJWT verify(String token) {

        return JWT.require(algorithm)
                .withIssuer("produto-auth-api")
                .build()
                .verify(token);
    }
}
