package com.medic.web.repository;

import com.medic.web.dto.usuario.UsuarioPaginaDTO;
import com.medic.web.model.usuario.Role;
import com.medic.web.model.usuario.UsuarioModel;
import com.medic.web.repository.usuario.UsuarioRepositoryCustomImpl;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;
    @Mock
    private RowMetadata metadata;

    @Test
    void shouldFilterSortAndPaginateRowsInDatabase() {

        UUID id = UUID.randomUUID();
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(row.get("id", UUID.class)).thenReturn(id);
        when(row.get("nome", String.class)).thenReturn("Ana");
        when(row.get("email", String.class)).thenReturn("ana@example.com");
        when(row.get("role", String.class)).thenReturn("ADMIN");
        when(row.get("ativo", Boolean.class)).thenReturn(false);
        stubUserRows();

        UsuarioPaginaDTO filtro = new UsuarioPaginaDTO();
        filtro.setNome(" Ana ");
        filtro.setEmail("ANA@example.com");
        filtro.setRole(Role.ADMIN);
        filtro.setAtivo(false);
        filtro.setOrdenarPor("NoMe");
        filtro.setDirecaoOrdenacao("DESC");

        StepVerifier.create(new UsuarioRepositoryCustomImpl(databaseClient).findPageByFilter(filtro, 10, 20))
                .expectNextMatches(usuario -> usuario.getId().equals(id)
                        && usuario.getNome().equals("Ana")
                        && usuario.getEmail().equals("ana@example.com")
                        && usuario.getRole() == Role.ADMIN
                        && !usuario.getAtivo())
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("LOWER(TRIM(u.nome)) = LOWER(TRIM(:nome))"));
        assertTrue(sql.getValue().contains("LOWER(TRIM(u.email)) = LOWER(TRIM(:email))"));
        assertTrue(sql.getValue().contains("u.role = :role"));
        assertTrue(sql.getValue().contains("u.ativo = :ativo"));
        assertTrue(sql.getValue().contains("ORDER BY LOWER(u.nome) DESC, u.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
        verify(executeSpec).bind("nome", " Ana ");
        verify(executeSpec).bind("email", "ANA@example.com");
        verify(executeSpec).bind("role", "ADMIN");
        verify(executeSpec).bind("ativo", false);
        verify(executeSpec).bind("limite", 10L);
        verify(executeSpec).bind("deslocamento", 20L);
    }

    @Test
    void shouldCountMatchingRowsWithTheSameFilters() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(row.get("total", Long.class)).thenReturn(5L);
        stubCountRows();

        UsuarioPaginaDTO filtro = new UsuarioPaginaDTO();
        filtro.setNome("Ana");
        filtro.setAtivo(true);

        StepVerifier.create(new UsuarioRepositoryCustomImpl(databaseClient).countByFilter(filtro))
                .expectNext(5L)
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("SELECT COUNT(*) AS total"));
        assertTrue(sql.getValue().contains("LOWER(TRIM(u.nome)) = LOWER(TRIM(:nome))"));
        assertTrue(sql.getValue().contains("u.ativo = :ativo"));
        verify(executeSpec).bind("nome", "Ana");
        verify(executeSpec).bind("ativo", true);
    }

    @Test
    void shouldUseStableDefaultOrderingWhenSortFieldIsOmitted() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        stubEmptyUserRows();

        StepVerifier.create(new UsuarioRepositoryCustomImpl(databaseClient)
                        .findPageByFilter(null, 10, 0))
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("ORDER BY u.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
    }

    @ParameterizedTest
    @MethodSource("supportedSortFields")
    void shouldUseOnlySupportedSortExpressions(String field, String expression) {

        DatabaseClient client = org.mockito.Mockito.mock(DatabaseClient.class);
        DatabaseClient.GenericExecuteSpec query = org.mockito.Mockito.mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<UsuarioModel> rows = org.mockito.Mockito.mock(RowsFetchSpec.class);
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, UsuarioModel>>any()))
                .thenReturn(rows);
        when(rows.all()).thenReturn(Flux.empty());

        UsuarioPaginaDTO filtro = new UsuarioPaginaDTO();
        filtro.setOrdenarPor(field);
        filtro.setDirecaoOrdenacao("ASC");

        StepVerifier.create(new UsuarioRepositoryCustomImpl(client).findPageByFilter(filtro, 10, 0))
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("ORDER BY " + expression));
    }

    private static Stream<Arguments> supportedSortFields() {

        return Stream.of(
                Arguments.of("nome", "LOWER(u.nome) ASC, u.id ASC"),
                Arguments.of("email", "LOWER(u.email) ASC, u.id ASC"),
                Arguments.of("role", "CASE u.role WHEN 'USER' THEN 0 WHEN 'MASTER' THEN 1 WHEN 'ADMIN' THEN 2 ELSE 3 END ASC, u.id ASC"),
                Arguments.of("ativo", "u.ativo ASC, u.id ASC")
        );
    }

    @Test
    void shouldRejectUnlistedSortFieldsAndDirections() {

        var repository = new UsuarioRepositoryCustomImpl(databaseClient);
        UsuarioPaginaDTO filtro = new UsuarioPaginaDTO();
        filtro.setOrdenarPor("id");

        assertThrows(IllegalArgumentException.class, () -> repository.findPageByFilter(filtro, 10, 0));

        filtro.setOrdenarPor("nome; DROP TABLE usuario");

        assertThrows(IllegalArgumentException.class, () -> repository.findPageByFilter(filtro, 10, 0));

        filtro.setOrdenarPor("nome");
        filtro.setDirecaoOrdenacao("DESC; DROP TABLE usuario");
        assertThrows(IllegalArgumentException.class, () -> repository.findPageByFilter(filtro, 10, 0));
        verifyNoInteractions(databaseClient);
    }

    private void stubUserRows() {

        RowsFetchSpec<UsuarioModel> rows = org.mockito.Mockito.mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, UsuarioModel>>any()))
                .thenAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, UsuarioModel> mapper = invocation.getArgument(0);
                    UsuarioModel usuario = mapper.apply(row, metadata);
                    when(rows.all()).thenReturn(Flux.just(usuario));
                    return rows;
                });
    }

    private void stubEmptyUserRows() {

        RowsFetchSpec<UsuarioModel> rows = org.mockito.Mockito.mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, UsuarioModel>>any()))
                .thenReturn(rows);
        when(rows.all()).thenReturn(Flux.empty());
    }

    private void stubCountRows() {

        RowsFetchSpec<Long> rows = org.mockito.Mockito.mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, Long> mapper = invocation.getArgument(0);
                    Long total = mapper.apply(row, metadata);
                    when(rows.one()).thenReturn(Mono.just(total));
                    return rows;
                });
    }
}
