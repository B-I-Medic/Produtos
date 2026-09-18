package com.medic.Web.repository.anvisa;

import com.medic.Web.dto.anvisa.AnvisaModeloDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoDTO;
import com.medic.Web.dto.anvisa.AnvisaProdutoLocalConsulta;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnvisaRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @Test
    void shouldMapTheOfficialProduct() {

        RowsFetchSpec<AnvisaProdutoDTO> rows = mockRows();
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("codAnvisa", "123")).thenReturn(executeSpec);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, AnvisaProdutoDTO>>any()))
                .thenReturn(rows);
        when(rows.all()).thenReturn(Flux.just(product()));

        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findProduto("123"))
                .assertNext(result -> assertEquals("123", result.numeroRegistroCadastro()))
                .verifyComplete();
    }

    @Test
    void shouldMapModelsCompaniesAndLocalProducts() {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);

        RowsFetchSpec<AnvisaModeloDTO> modelRows = mockRows();
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, AnvisaModeloDTO>>any()))
                .thenReturn(modelRows);
        when(modelRows.all()).thenReturn(Flux.just(new AnvisaModeloDTO(
                "123", "Tecnico", "II", "Comercial", "Detentor", "Fabricante", "Brasil", "M1", "2026", "2030"
        )));
        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findModelos("123"))
                .assertNext(result -> assertEquals("M1", result.dsModeloProdutoMedico()))
                .verifyComplete();

        RowsFetchSpec<AnvisaRepositoryCustom.EmpresaConsulta> companyRows = mockRows();
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, AnvisaRepositoryCustom.EmpresaConsulta>>any()))
                .thenReturn(companyRows);
        when(companyRows.all()).thenReturn(Flux.just(
                new AnvisaRepositoryCustom.EmpresaConsulta("Empresa", "UFX", "01")
        ));
        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findEmpresas())
                .assertNext(result -> assertEquals("Empresa", result.descricao()))
                .verifyComplete();

        RowsFetchSpec<AnvisaProdutoLocalConsulta> localRows = mockRows();
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, AnvisaProdutoLocalConsulta>>any()))
                .thenReturn(localRows);
        when(localRows.all()).thenReturn(Flux.just(
                new AnvisaProdutoLocalConsulta("UFX", "01", "P1", "Produto")
        ));
        StepVerifier.create(new AnvisaRepositoryCustomImpl(databaseClient).findProdutosLocais("123"))
                .assertNext(result -> assertEquals("P1", result.codProduto()))
                .verifyComplete();
    }

    private AnvisaProdutoDTO product() {

        return new AnvisaProdutoDTO(
                "123", "PROC", "Tecnico", "II", "Comercial", "CNPJ", "Detentor", "Fabricante", "Brasil", "2026", "2030"
        );
    }

    private <T> RowsFetchSpec<T> mockRows() {

        return mock(RowsFetchSpec.class);
    }
}
