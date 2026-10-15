package com.medic.web.repository.empresa;

import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.model.empresa.Viman;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaRepositoryMappingTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private Row row;

    @Test
    void shouldMapEmpresaAndMunicipioFromSingleJoinQuery() {

        UUID empresaId = UUID.randomUUID();
        UUID municipioId = UUID.randomUUID();
        prepareQuery();
        when(row.get("empresa_id", UUID.class)).thenReturn(empresaId);
        when(row.get("empresa_descricao", String.class)).thenReturn("Empresa Central");
        when(row.get("empresa_viman", String.class)).thenReturn("UFX");
        when(row.get("empresa_codigo_empresa", String.class)).thenReturn("001");
        when(row.get("empresa_possui_estoque_interno", Boolean.class)).thenReturn(true);
        when(row.get("empresa_possui_estoque_segregado", Boolean.class)).thenReturn(false);
        when(row.get("empresa_possui_vp", Boolean.class)).thenReturn(true);
        when(row.get("municipio_id", UUID.class)).thenReturn(municipioId);
        when(row.get("municipio_descricao", String.class)).thenReturn("Cidade");
        when(row.get("municipio_cod_ibge", String.class)).thenReturn("1234567");
        when(row.get("municipio_estado", String.class)).thenReturn("SP");
        stubMappedRows();

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient).findAllWithMunicipio())
                .expectNextMatches(response -> response.id().equals(empresaId)
                        && response.descricao().equals("Empresa Central")
                        && response.viman() == Viman.UFX
                        && response.codigoEmpresa().equals("001")
                        && response.possuiEstoqueInterno()
                        && !response.possuiEstoqueSegregado()
                        && response.possuiVp()
                        && response.municipio() != null
                        && response.municipio().id().equals(municipioId)
                        && response.municipio().descricao().equals("Cidade")
                        && response.municipio().codigoIbge().equals("1234567")
                        && response.municipio().estado().equals("SP")
                        && response.municipio().centroDistribuicao() == null)
                .verifyComplete();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sql.capture());
        assertTrue(sql.getValue().contains("LEFT JOIN municipio m"));
        assertTrue(sql.getValue().contains("ON m.id = e.municipio_id"));
    }

    @Test
    void shouldMapEmpresaWithoutMunicipioAndNullableValues() {

        UUID empresaId = UUID.randomUUID();
        prepareQuery();
        when(row.get("empresa_id", UUID.class)).thenReturn(empresaId);
        when(row.get("empresa_descricao", String.class)).thenReturn("Empresa sem município");
        when(row.get("empresa_viman", String.class)).thenReturn(null);
        when(row.get("empresa_codigo_empresa", String.class)).thenReturn("002");
        when(row.get("empresa_possui_estoque_interno", Boolean.class)).thenReturn(null);
        when(row.get("empresa_possui_estoque_segregado", Boolean.class)).thenReturn(false);
        when(row.get("empresa_possui_vp", Boolean.class)).thenReturn(null);
        when(row.get("municipio_id", UUID.class)).thenReturn(null);
        stubMappedRows();

        StepVerifier.create(new EmpresaRepositoryCustomImpl(databaseClient).findAllWithMunicipio())
                .expectNextMatches(response -> response.id().equals(empresaId)
                        && response.descricao().equals("Empresa sem município")
                        && response.viman() == null
                        && response.codigoEmpresa().equals("002")
                        && !response.possuiEstoqueInterno()
                        && !response.possuiEstoqueSegregado()
                        && !response.possuiVp()
                        && response.municipio() == null)
                .verifyComplete();

        verify(databaseClient).sql(anyString());
    }

    private void prepareQuery() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
    }

    private void stubMappedRows() {

        RowsFetchSpec<EmpresaResponseDTO> rows = mock(RowsFetchSpec.class);
        doAnswer(invocation -> {
                    BiFunction<Row, RowMetadata, EmpresaResponseDTO> mapper = invocation.getArgument(0);
                    EmpresaResponseDTO mapped = mapper.apply(row, null);
                    when(rows.all()).thenReturn(Flux.just(mapped));
                    return rows;
                })
                .when(executeSpec)
                .map(ArgumentMatchers.<BiFunction<Row, RowMetadata, EmpresaResponseDTO>>any());
    }
}
