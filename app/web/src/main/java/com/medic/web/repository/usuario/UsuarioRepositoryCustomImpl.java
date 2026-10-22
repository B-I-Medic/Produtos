package com.medic.web.repository.usuario;

import com.medic.web.dto.usuario.UsuarioPaginaDTO;
import com.medic.web.model.usuario.Role;
import com.medic.web.model.usuario.UsuarioModel;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Repository
public class UsuarioRepositoryCustomImpl implements UsuarioRepositoryCustom {

    private static final String SELECT_PAGE = """
            SELECT u.id, u.nome, u.email, u.role, u.ativo
              FROM usuario u
            """;

    private static final String SELECT_COUNT = """
            SELECT COUNT(*) AS total
              FROM usuario u
            """;

    private static final Map<String, String> ORDER_BY_COLUMNS = Map.of(
            "id", "u.id",
            "nome", "LOWER(u.nome)",
            "email", "LOWER(u.email)",
            "role", "CASE u.role WHEN 'USER' THEN 0 WHEN 'MASTER' THEN 1 WHEN 'ADMIN' THEN 2 ELSE 3 END",
            "ativo", "u.ativo"
    );

    private final DatabaseClient databaseClient;

    public UsuarioRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<UsuarioModel> findPageByFilter(UsuarioPaginaDTO filtro, long limite, long deslocamento) {

        String orderBy = orderBy(filtro);
        String where = where(filtro);
        String sql = SELECT_PAGE + where + " ORDER BY " + orderBy + " LIMIT :limite OFFSET :deslocamento";

        var query = bindFilters(databaseClient.sql(sql), filtro)
                .bind("limite", limite)
                .bind("deslocamento", deslocamento);

        return query.map((row, metadata) -> {
                    var usuario = new UsuarioModel();
                    usuario.setId(row.get("id", java.util.UUID.class));
                    usuario.setNome(row.get("nome", String.class));
                    usuario.setEmail(row.get("email", String.class));
                    usuario.setRole(Role.valueOf(Objects.requireNonNull(row.get("role", String.class))));
                    usuario.setAtivo(row.get("ativo", Boolean.class));
                    return usuario;
                })
                .all();
    }

    @Override
    public Mono<Long> countByFilter(UsuarioPaginaDTO filtro) {

        String sql = SELECT_COUNT + where(filtro);

        return bindFilters(databaseClient.sql(sql), filtro)
                .map((row, metadata) -> Objects.requireNonNull(row.get("total", Long.class)))
                .one();
    }

    private DatabaseClient.GenericExecuteSpec bindFilters(DatabaseClient.GenericExecuteSpec query,
                                                          UsuarioPaginaDTO filtro) {

        if (filtro == null) {
            return query;
        }

        if (filtro.getNome() != null) {
            query = query.bind("nome", filtro.getNome());
        }
        if (filtro.getEmail() != null) {
            query = query.bind("email", filtro.getEmail());
        }
        if (filtro.getRole() != null) {
            query = query.bind("role", filtro.getRole().name());
        }
        if (filtro.getAtivo() != null) {
            query = query.bind("ativo", filtro.getAtivo());
        }

        return query;
    }

    private String where(UsuarioPaginaDTO filtro) {

        if (filtro == null) {
            return "";
        }

        List<String> conditions = new ArrayList<>();
        if (filtro.getNome() != null) {
            conditions.add("LOWER(TRIM(u.nome)) = LOWER(TRIM(:nome))");
        }
        if (filtro.getEmail() != null) {
            conditions.add("LOWER(TRIM(u.email)) = LOWER(TRIM(:email))");
        }
        if (filtro.getRole() != null) {
            conditions.add("u.role = :role");
        }
        if (filtro.getAtivo() != null) {
            conditions.add("u.ativo = :ativo");
        }

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private String orderBy(UsuarioPaginaDTO filtro) {

        String field = filtro == null || filtro.getOrdenarPor() == null
                ? "id"
                : filtro.getOrdenarPor().toLowerCase(Locale.ROOT);

        String column = ORDER_BY_COLUMNS.get(field);
        if (column == null) {
            throw new IllegalArgumentException("Campo inválido para ordenação de usuários");
        }

        String direction = filtro == null || filtro.getDirecaoOrdenacao() == null
                ? "ASC"
                : filtro.getDirecaoOrdenacao().toUpperCase(Locale.ROOT);
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw new IllegalArgumentException("A direção da ordenação deve ser ASC ou DESC");
        }

        String desempateId = "id".equals(field) ? "" : ", u.id ASC";
        return column + " " + direction + desempateId;
    }
}
