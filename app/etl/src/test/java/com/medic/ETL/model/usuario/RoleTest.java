package com.medic.ETL.model.usuario;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class RoleTest {

    @Test
    void shouldExposeAllSupportedRoles() {

        assertArrayEquals(new Role[]{Role.USER, Role.MASTER, Role.ADMIN}, Role.values());
    }
}
