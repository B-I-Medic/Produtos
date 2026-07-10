package com.medic.Web.repository.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeAgrupadoResponseDTO;
import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.model.necessidade.AgrupamentosPadrao;
import com.medic.Web.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NecessidadeRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;
    @Mock
    private RowsFetchSpec<NecessidadeAgrupadoResponseDTO> rowsFetchSpec;

    @Test
    void shouldUseDefaultGroupByWhenFilterIsNull() {

        var response = TestDataFactory.necessidadeAgrupadoResponseDTO();
        stubQuery(response);

        var repository = new NecessidadeRepositoryCustomImpl(databaseClient);

        StepVerifier.create(repository.findByFilter(null))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("group by centro_distribuicao, empresa, municipio, anvisa, marca, cod_produto, produto"));
        verify(executeSpec).bindNull("centro_distribuicao", String.class);
        verify(executeSpec).bindNull("empresa", String.class);
        verify(executeSpec).bindNull("municipio", String.class);
        verify(executeSpec).bindNull("produto", String.class);
        verify(executeSpec).bindNull("marca", String.class);
    }

    @Test
    void shouldBindFiltersAndCustomGroupBy() {

        var response = TestDataFactory.necessidadeAgrupadoResponseDTO();
        stubQuery(response);

        var repository = new NecessidadeRepositoryCustomImpl(databaseClient);
        var filter = new NecessidadeFilterDTO(
                "CD",
                "Empresa",
                "Cidade",
                "Produto",
                "Marca",
                List.of(AgrupamentosPadrao.EMPRESA, AgrupamentosPadrao.MUNICIPIO)
        );

        StepVerifier.create(repository.findByFilter(filter))
                .expectNext(response)
                .verifyComplete();

        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("select"));
        assertTrue(sqlCaptor.getValue().contains("group by empresa, municipio"));
        verify(executeSpec).bind("centro_distribuicao", "%CD%");
        verify(executeSpec).bind("empresa", "%Empresa%");
        verify(executeSpec).bind("municipio", "%Cidade%");
        verify(executeSpec).bind("produto", "%Produto%");
        verify(executeSpec).bind("marca", "%Marca%");
    }

    private void stubQuery(NecessidadeAgrupadoResponseDTO response) {

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        lenient().when(executeSpec.bindNull(anyString(), eq(String.class))).thenReturn(executeSpec);
        when(executeSpec.map(org.mockito.ArgumentMatchers.<BiFunction<Row, RowMetadata, NecessidadeAgrupadoResponseDTO>>any()))
                .thenReturn(rowsFetchSpec);
        when(rowsFetchSpec.all()).thenReturn(Flux.just(response));
    }
}
