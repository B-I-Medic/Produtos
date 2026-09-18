package com.medic.ETL.service.anvisa;

import com.medic.ETL.model.processamento.Processamento;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnvisaCargaServiceTest {

    @Test
    void shouldLoadProductsAndModelsInBatchesAndActivateTheNewLoad() throws Exception {

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AnvisaCsvReader csvReader = mock(AnvisaCsvReader.class);
        AnvisaModeloSplitter splitter = mock(AnvisaModeloSplitter.class);
        Path produtos = Path.of("produtos.csv");
        Path modelos = Path.of("modelos.csv");

        doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            Consumer<Map<String, String>> consumer = invocation.getArgument(2);
            int total = path.equals(produtos) ? 500 : 500;
            for (int index = 0; index < total; index++) {
                consumer.accept(path.equals(produtos) ? productValues(index) : modelValues(index));
            }
            return null;
        }).when(csvReader).read(any(Path.class), anySet(), any());
        when(splitter.split(any())).thenReturn(java.util.List.of("MODELO-1"));
        when(jdbcTemplate.batchUpdate(any(String.class), ArgumentMatchers.<java.util.List<Object[]>>any()))
                .thenReturn(new int[]{1});
        doAnswer(invocation -> {
            PreparedStatementSetter setter = invocation.getArgument(1);
            setter.setValues(mock(PreparedStatement.class));
            return 1;
        }).when(jdbcTemplate).update(any(String.class), any(PreparedStatementSetter.class));

        Processamento processamento = new Processamento();
        processamento.setId(java.util.UUID.randomUUID());
        new AnvisaCargaService(jdbcTemplate, csvReader, splitter)
                .promover(processamento, produtos, modelos);
    }

    private Map<String, String> productValues(int index) {

        Map<String, String> values = new HashMap<>();
        values.put("NUMERO_REGISTRO_CADASTRO", "REG-" + index);
        values.put("NUMERO_PROCESSO", "PROC-" + index);
        values.put("NOME_TECNICO", "Tecnico");
        values.put("CLASSE_RISCO", "II");
        values.put("NOME_COMERCIAL", "Comercial");
        values.put("CNPJ_DETENTOR_REGISTRO_CADASTRO", "CNPJ");
        values.put("DETENTOR_REGISTRO_CADASTRO", "Detentor");
        values.put("NOME_FABRICANTE", "Fabricante");
        values.put("NOME_PAIS_FABRIC", "Brasil");
        values.put("DT_PUB_REGISTRO_CADASTRO", "01/01/2026");
        values.put("VALIDADE_REGISTRO_CADASTRO", "01/01/2030");
        values.put("DT_ATUALIZACAO_DADO", "01/01/2026");
        return values;
    }

    private Map<String, String> modelValues(int index) {

        Map<String, String> values = new HashMap<>();
        values.put("NUMERO_REGISTRO_CADASTRO", "REG-" + index);
        values.put("NOME_TECNICO", "Tecnico");
        values.put("CLASSE_RISCO", "II");
        values.put("NOME_COMERCIAL", "Comercial");
        values.put("DETENTOR_REGISTRO_CADASTRO", "Detentor");
        values.put("NOME_FABRICANTE", "Fabricante");
        values.put("NOME_PAIS_FABRIC", "Brasil");
        values.put("DS_MODELO_PRODUTO_MEDICO", "MODELO-1");
        values.put("DT_PUB_REGISTRO_CADASTRO", "01/01/2026");
        values.put("VALIDADE_REGISTRO_CADASTRO", "01/01/2030");
        values.put("DT_ATUALIZACAO_DADO", "01/01/2026");
        return values;
    }
}
