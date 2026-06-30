package com.medic.Web.mapper.auth;

import com.medic.Web.model.auth.PasswordResetCodeModel;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PasswordResetCodeMapper {

    public PasswordResetCodeModel toEntity(String email, String codigo, Instant expiracaEm) {

        var entity = new PasswordResetCodeModel();

        entity.setEmail(email);
        entity.setCodigo(codigo);
        entity.setCriadoEm(Instant.now());
        entity.setExpiraEm(expiracaEm);
        entity.setUsado(false);

        return entity;
    }
}
