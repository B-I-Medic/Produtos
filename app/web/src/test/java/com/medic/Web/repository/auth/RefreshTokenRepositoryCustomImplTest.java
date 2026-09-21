package com.medic.Web.repository.auth;

import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenRepositoryCustomImplTest {

    @Test
    void shouldExecuteAllRefreshTokenStateChanges() {

        DatabaseClient client = mock(DatabaseClient.class);
        DatabaseClient.GenericExecuteSpec executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        FetchSpec<Map<String, Object>> fetchSpec = mock(FetchSpec.class);
        when(client.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L), Mono.just(2L), Mono.just(0L));

        RefreshTokenRepositoryCustomImpl repository = new RefreshTokenRepositoryCustomImpl(client);
        StepVerifier.create(repository.consumeIfActive(UUID.randomUUID(), Instant.now()))
                .expectNext(true).verifyComplete();
        StepVerifier.create(repository.revokeByFamilyId(UUID.randomUUID(), Instant.now()))
                .expectNext(2L).verifyComplete();
        StepVerifier.create(repository.revokeByUsuarioId(UUID.randomUUID(), Instant.now()))
                .expectNext(0L).verifyComplete();

        verify(client, org.mockito.Mockito.times(3)).sql(anyString());
        assertEquals(3, org.mockito.Mockito.mockingDetails(executeSpec).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("fetch"))
                .count());
    }
}
