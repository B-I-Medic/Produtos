package com.medic.web.repository.empresa;

import com.medic.web.dto.empresa.EmpresaPaginaDTO;
import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.model.empresa.Viman;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;
    @Mock
    private RowMetadata metadata;

    @Test
    void shouldFilterAndPageCompaniesOrderingByMunicipalityDescription() {

        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        preparePageQuery(empresaId, municipioId);

        EmpresaPaginaDTO filtro = allFilters(municipioId);
        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC");

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient).findPageByFilter(filtro, 5, 10))
                .expectNextMatches(response -> response.id().equals(empresaId)
                        && response.descricao().equals("Empresa Central")
                        && response.viman() == Viman.UFX
                        && response.codigoEmpresa().equals("001")
                        && response.possuiEstoqueInterno()
                        && !response.possuiEstoqueSegregado()
                        && response.possuiVp()
                        && response.municipio().id().equals(municipioId)
                        && response.municipio().descricao().equals("Campinas"))
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("LOWER(TRIM(e.descricao)) = LOWER(TRIM(:descricao))"));
        assertTrue(sql.getValue().contains("LOWER(TRIM(e.codigo_empresa)) = LOWER(TRIM(:codigo))"));
        assertTrue(sql.getValue().contains("e.viman = :viman"));
        assertTrue(sql.getValue().contains("e.possui_estoque_interno = :possuiEstoqueInterno"));
        assertTrue(sql.getValue().contains("e.possui_estoque_segregado = :possuiEstoqueSegregado"));
        assertTrue(sql.getValue().contains("e.possui_vp = :possuiVp"));
        assertTrue(sql.getValue().contains("e.municipio_id = :municipio"));
        assertTrue(sql.getValue().contains("ORDER BY LOWER(m.descricao) DESC, e.id ASC"));
        assertTrue(sql.getValue().contains("LIMIT :limite OFFSET :deslocamento"));
        verify(executeSpec).bind("descricao", "Empresa Central");
        verify(executeSpec).bind("codigo", "001");
        verify(executeSpec).bind("viman", "UFX");
        verify(executeSpec).bind("possuiEstoqueInterno", true);
        verify(executeSpec).bind("possuiEstoqueSegregado", false);
        verify(executeSpec).bind("possuiVp", true);
        verify(executeSpec).bind("municipio", municipioId);
        verify(executeSpec).bind("limite", 5L);
        verify(executeSpec).bind("deslocamento", 10L);
    }

    @Test
    void shouldCountAllCompaniesMatchingTheFilter() {

        UUID municipioId = UUID.randomUUID();
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(row.get("total", Long.class)).thenReturn(8L);
        RowsFetchSpec<Long> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, Long>>any()))
                .thenAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, Long> mapper = invocation.getArgument(0);
                    Long count = mapper.apply(row, metadata);
                    when(rows.one()).thenReturn(Mono.just(count));
                    return rows;
                });

        EmpresaPaginaDTO filtro = allFilters(municipioId);

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient).countByFilter(filtro))
                .expectNext(8L)
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("SELECT COUNT(*) AS total"));
        assertTrue(sql.getValue().contains("e.municipio_id = :municipio"));
        verify(executeSpec).bind("municipio", municipioId);
        verify(executeSpec).bind("possuiVp", true);
    }

    @ParameterizedTest
    @MethodSource("supportedSortFields")
    void shouldUseWhitelistedCompanySortExpressions(String field, String expression) {

        DatabaseClient client = mock(DatabaseClient.class);
        DatabaseClient.GenericExecuteSpec query = mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<EmpresaResponseDTO> rows = mock(RowsFetchSpec.class);
        when(client.sql(anyString())).thenReturn(query);
        when(query.bind(anyString(), any())).thenReturn(query);
        when(query.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, EmpresaResponseDTO>>any()))
                .thenReturn(rows);
        when(rows.all()).thenReturn(Flux.empty());

        EmpresaPaginaDTO filtro = new EmpresaPaginaDTO();
        filtro.setOrdenarPor(field);
        filtro.setDirecaoOrdenacao("ASC");

        StepVerifier.create(new EmpresaRepositoryCustomImpl(client).findPageByFilter(filtro, 10, 0))
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(client).sql(sql.capture());
        assertTrue(sql.getValue().contains("ORDER BY " + expression));
    }

    @Test
    void shouldRejectUnlistedSortFieldsAndDirections() {

        var repository = new EmpresaRepositoryCustomImpl(databaseClient);
        EmpresaPaginaDTO filtro = new EmpresaPaginaDTO();
        filtro.setOrdenarPor("municipio; DROP TABLE empresa");

        assertThrows(IllegalArgumentException.class, () -> repository.findPageByFilter(filtro, 10, 0));

        filtro.setOrdenarPor("municipio");
        filtro.setDirecaoOrdenacao("DESC; DROP TABLE empresa");
        assertThrows(IllegalArgumentException.class, () -> repository.findPageByFilter(filtro, 10, 0));
        verifyNoInteractions(databaseClient);
    }

    private EmpresaPaginaDTO allFilters(UUID municipioId) {

        EmpresaPaginaDTO filtro = new EmpresaPaginaDTO();
        filtro.setDescricao("Empresa Central");
        filtro.setCodigo("001");
        filtro.setViman(Viman.UFX);
        filtro.setPossuiEstoqueInterno(true);
        filtro.setPossuiEstoqueSegregado(false);
        filtro.setPossuiVp(true);
        filtro.setMunicipio(municipioId);
        return filtro;
    }

    private void preparePageQuery(UUID empresaId, UUID municipioId) {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(row.get("empresa_id", UUID.class)).thenReturn(empresaId);
        when(row.get("empresa_descricao", String.class)).thenReturn("Empresa Central");
        when(row.get("empresa_viman", String.class)).thenReturn("UFX");
        when(row.get("empresa_codigo_empresa", String.class)).thenReturn("001");
        when(row.get("empresa_possui_estoque_interno", Boolean.class)).thenReturn(true);
        when(row.get("empresa_possui_estoque_segregado", Boolean.class)).thenReturn(false);
        when(row.get("empresa_possui_vp", Boolean.class)).thenReturn(true);
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Campinas");
        when(row.get("municipio_cod_ibge", String.class)).thenReturn("1234567");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");

        RowsFetchSpec<EmpresaResponseDTO> rows = mock(RowsFetchSpec.class);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, EmpresaResponseDTO>>any()))
                .thenAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, EmpresaResponseDTO> mapper = invocation.getArgument(0);
                    EmpresaResponseDTO response = mapper.apply(row, metadata);
                    when(rows.all()).thenReturn(Flux.just(response));
                    return rows;
                });
    }

    private static Stream<Arguments> supportedSortFields() {

        return Stream.of(
                Arguments.of("descricao", "LOWER(e.descricao) ASC, e.id ASC"),
                Arguments.of("codigo", "LOWER(e.codigo_empresa) ASC, e.id ASC"),
                Arguments.of("viman", "e.viman ASC, e.id ASC"),
                Arguments.of("possui_estoque_interno", "e.possui_estoque_interno ASC, e.id ASC"),
                Arguments.of("possui_estoque_segregado", "e.possui_estoque_segregado ASC, e.id ASC"),
                Arguments.of("possui_vp", "e.possui_vp ASC, e.id ASC"),
                Arguments.of("municipio", "LOWER(m.descricao) ASC, e.id ASC")
        );
    }
}
