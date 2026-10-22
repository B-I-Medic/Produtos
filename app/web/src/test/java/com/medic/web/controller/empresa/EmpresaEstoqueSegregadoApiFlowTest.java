package com.medic.web.controller.empresa;

import com.medic.web.dto.config.estoque.segregado.EstoqueSegregadoResponseDTO;
import com.medic.web.repository.municipio.MunicipioRepository;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepository;
import com.medic.web.repository.config.estoque.segregado.EstoqueSegregadoRepositoryCustomImpl;
import com.medic.web.service.config.estoque.ManutencaoEstoqueSegregadoService;
import com.medic.web.service.empresa.ManutencaoEmpresaService;
import com.medic.web.service.empresa.ConsultaEmpresaService;
import com.medic.web.validator.estoque.ValidadorEmpresaEstoqueService;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpresaEstoqueSegregadoApiFlowTest {

    @Test
    void shouldReturnCompanyStockThroughControllerServiceAndRepository() {

        UUID empresaId = UUID.randomUUID();
        UUID estoqueId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();

        DatabaseClient databaseClient = mock(DatabaseClient.class);
        DatabaseClient.GenericExecuteSpec executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        @SuppressWarnings("unchecked")
        RowsFetchSpec<EstoqueSegregadoResponseDTO> rows = mock(RowsFetchSpec.class);
        Row row = mock(Row.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.bindNull(anyString(), eq(String.class))).thenReturn(executeSpec);
        when(executeSpec.bindNull(anyString(), eq(Integer.class))).thenReturn(executeSpec);
        when(executeSpec.bindNull(anyString(), eq(UUID.class))).thenReturn(executeSpec);
        when(row.get("id", UUID.class)).thenReturn(estoqueId);
        when(row.get("cod_segregado", Integer.class)).thenReturn(42);
        when(row.get("descricao", String.class)).thenReturn("Estoque de teste");
        when(row.get("ativo", Boolean.class)).thenReturn(true);
        when(row.get("empresa_id", UUID.class)).thenReturn(empresaId);
        when(row.get("empresa_descricao", String.class)).thenReturn("Empresa de teste");
        when(row.get("empresa_viman", String.class)).thenReturn("UFX");
        when(row.get("empresa_codigo_empresa", String.class)).thenReturn("001");
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Cidade de teste");
        when(row.get("municipio_codigo_ibge", String.class)).thenReturn("123");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            BiFunction<Row, RowMetadata, EstoqueSegregadoResponseDTO> mapper = invocation.getArgument(0);
            var response = mapper.apply(row, null);
            when(rows.all()).thenReturn(Flux.just(response));
            return rows;
        }).when(executeSpec).map(org.mockito.ArgumentMatchers
                .<BiFunction<Row, RowMetadata, EstoqueSegregadoResponseDTO>>any());

        var customRepository = new EstoqueSegregadoRepositoryCustomImpl(databaseClient);
        EstoqueSegregadoRepository repository = mock(EstoqueSegregadoRepository.class);
        when(repository.getAllByIdEmpresa(empresaId))
                .thenAnswer(invocation -> customRepository.getAllByIdEmpresa(invocation.getArgument(0)));

        var service = new ManutencaoEstoqueSegregadoService(
                mock(MunicipioRepository.class),
                repository,
                mock(ValidadorEmpresaEstoqueService.class));
        var controller = new EmpresaController(mock(ManutencaoEmpresaService.class),
                mock(ConsultaEmpresaService.class), service);
        var client = WebTestClient.bindToController(controller).build();

        var result = client.get()
                .uri("/empresa/{empresaId}/estoque-segregado/get", empresaId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(EstoqueSegregadoResponseDTO.class);

        StepVerifier.create(result.getResponseBody())
                .expectNextMatches(response -> response.id().equals(estoqueId)
                        && response.codSegregado() == 42
                        && response.empresa().id().equals(empresaId)
                        && response.municipio().id().equals(municipioId))
                .verifyComplete();

        verify(repository).getAllByIdEmpresa(empresaId);
        verify(executeSpec).bind("idEmpresa", empresaId);
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("esp.id_empresa = :idEmpresa"));
    }
}
