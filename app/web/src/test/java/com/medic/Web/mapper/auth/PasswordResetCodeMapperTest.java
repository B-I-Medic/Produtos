package com.medic.Web.mapper.auth;

import com.medic.Web.model.auth.PasswordResetCodeModel;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetCodeMapperTest {

    private final PasswordResetCodeMapper mapper = new PasswordResetCodeMapper();

    @Test
    void shouldMapPasswordResetCodeWithAuditDefaults() {

        Instant before = Instant.now();
        Instant expiration = before.plusSeconds(600);

        PasswordResetCodeModel entity = mapper.toEntity("teste@medic.com", "123456", expiration);

        Instant after = Instant.now();

        assertNull(entity.getId());
        assertEquals("teste@medic.com", entity.getEmail());
        assertEquals("123456", entity.getCodigo());
        assertEquals(expiration, entity.getExpiraEm());
        assertFalse(entity.isUsado());
        assertNotNull(entity.getCriadoEm());
        assertFalse(entity.getCriadoEm().isBefore(before));
        assertFalse(entity.getCriadoEm().isAfter(after));
    }
}
