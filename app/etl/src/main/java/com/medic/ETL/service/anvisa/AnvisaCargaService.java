package com.medic.ETL.service.anvisa;

import com.medic.ETL.model.processamento.Processamento;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AnvisaCargaService {

    private static final int BATCH_SIZE = 500;

    private static final Set<String> PRODUTO_HEADERS = Set.of(
            "NUMERO_REGISTRO_CADASTRO",
            "NUMERO_PROCESSO",
            "NOME_TECNICO",
            "CLASSE_RISCO",
            "NOME_COMERCIAL",
            "CNPJ_DETENTOR_REGISTRO_CADASTRO",
            "DETENTOR_REGISTRO_CADASTRO",
            "NOME_FABRICANTE",
            "NOME_PAIS_FABRIC",
            "DT_PUB_REGISTRO_CADASTRO",
            "VALIDADE_REGISTRO_CADASTRO",
            "DT_ATUALIZACAO_DADO"
    );

    private static final Set<String> MODELO_HEADERS = Set.of(
            "NUMERO_REGISTRO_CADASTRO",
            "NOME_TECNICO",
            "CLASSE_RISCO",
            "NOME_COMERCIAL",
            "DETENTOR_REGISTRO_CADASTRO",
            "NOME_FABRICANTE",
            "NOME_PAIS_FABRIC",
            "DS_MODELO_PRODUTO_MEDICO",
            "DT_PUB_REGISTRO_CADASTRO",
            "VALIDADE_REGISTRO_CADASTRO",
            "DT_ATUALIZACAO_DADO"
    );

    private final JdbcTemplate jdbcTemplate;
    private final AnvisaCsvReader csvReader;
    private final AnvisaModeloSplitter modeloSplitter;

    public AnvisaCargaService(@Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate,
                              AnvisaCsvReader csvReader,
                              AnvisaModeloSplitter modeloSplitter) {
        this.jdbcTemplate = jdbcTemplate;
        this.csvReader = csvReader;
        this.modeloSplitter = modeloSplitter;
    }

    @Transactional(rollbackFor = Exception.class)
    public void promover(Processamento processamento, Path arquivoProdutos, Path arquivoModelos) throws IOException {

        UUID cargaId = UUID.randomUUID();

        jdbcTemplate.update("""
                        insert into anvisa_carga (id, processamento_id, criado_em, ativa, produtos_count, modelos_count)
                        values (?, ?, ?, false, 0, 0)
                        """,
                preparedStatement -> {
                    preparedStatement.setObject(1, cargaId);
                    preparedStatement.setObject(2, processamento.getId());
                    preparedStatement.setObject(3, OffsetDateTime.now(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
                }
        );

        AtomicInteger produtos = new AtomicInteger();
        List<Object[]> loteProdutos = new ArrayList<>(BATCH_SIZE);

        csvReader.read(arquivoProdutos, PRODUTO_HEADERS, values -> {
            loteProdutos.add(parametrosProduto(cargaId, values));
            produtos.incrementAndGet();

            if (loteProdutos.size() == BATCH_SIZE) {
                inserirProdutos(loteProdutos);
                loteProdutos.clear();
            }
        });
        inserirProdutos(loteProdutos);

        AtomicInteger modelos = new AtomicInteger();
        Map<String, Integer> ordensPorRegistro = new java.util.HashMap<>();
        List<Object[]> loteModelos = new ArrayList<>(BATCH_SIZE);

        csvReader.read(arquivoModelos, MODELO_HEADERS, values -> {
            inserirModelos(cargaId, values, modelos, ordensPorRegistro, loteModelos);

            if (loteModelos.size() >= BATCH_SIZE) {
                inserirModelos(loteModelos);
                loteModelos.clear();
            }
        });
        inserirModelos(loteModelos);

        jdbcTemplate.update("""
                update anvisa_carga
                   set ativa = false
                 where id <> ?
                   and ativa = true
                """, cargaId);

        jdbcTemplate.update("""
                update anvisa_carga
                   set produtos_count = ?, modelos_count = ?, ativa = true
                 where id = ?
                """, produtos.get(), modelos.get(), cargaId);

        jdbcTemplate.update("delete from anvisa_carga where ativa = false");
    }

    private Object[] parametrosProduto(UUID cargaId, Map<String, String> values) {

        return new Object[]{
                cargaId,
                AnvisaCsvReader.value(values, "NUMERO_REGISTRO_CADASTRO"),
                AnvisaCsvReader.value(values, "NUMERO_PROCESSO"),
                AnvisaCsvReader.value(values, "NOME_TECNICO"),
                AnvisaCsvReader.value(values, "CLASSE_RISCO"),
                AnvisaCsvReader.value(values, "NOME_COMERCIAL"),
                AnvisaCsvReader.value(values, "CNPJ_DETENTOR_REGISTRO_CADASTRO"),
                AnvisaCsvReader.value(values, "DETENTOR_REGISTRO_CADASTRO"),
                AnvisaCsvReader.value(values, "NOME_FABRICANTE"),
                AnvisaCsvReader.value(values, "NOME_PAIS_FABRIC"),
                AnvisaCsvReader.value(values, "DT_PUB_REGISTRO_CADASTRO"),
                AnvisaCsvReader.value(values, "VALIDADE_REGISTRO_CADASTRO"),
                AnvisaCsvReader.value(values, "DT_ATUALIZACAO_DADO")
        };

    }

    private void inserirProdutos(List<Object[]> lote) {

        if (lote.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                insert into anvisa_produto (
                    carga_id, numero_registro_cadastro, numero_processo, nome_tecnico,
                    classe_risco, nome_comercial, cnpj_detentor_registro_cadastro,
                    detentor_registro_cadastro, nome_fabricante, nome_pais_fabric,
                    dt_pub_registro_cadastro, validade_registro_cadastro, dt_atualizacao_dado
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (carga_id, numero_registro_cadastro) do update set
                    numero_processo = excluded.numero_processo,
                    nome_tecnico = excluded.nome_tecnico,
                    classe_risco = excluded.classe_risco,
                    nome_comercial = excluded.nome_comercial,
                    cnpj_detentor_registro_cadastro = excluded.cnpj_detentor_registro_cadastro,
                    detentor_registro_cadastro = excluded.detentor_registro_cadastro,
                    nome_fabricante = excluded.nome_fabricante,
                    nome_pais_fabric = excluded.nome_pais_fabric,
                    dt_pub_registro_cadastro = excluded.dt_pub_registro_cadastro,
                    validade_registro_cadastro = excluded.validade_registro_cadastro,
                    dt_atualizacao_dado = excluded.dt_atualizacao_dado
                """, lote);
    }

    private void inserirModelos(UUID cargaId,
                                Map<String, String> values,
                                AtomicInteger count,
                                Map<String, Integer> ordensPorRegistro,
                                List<Object[]> lote) {

        String registro = AnvisaCsvReader.value(values, "NUMERO_REGISTRO_CADASTRO");
        String descricao = AnvisaCsvReader.value(values, "DS_MODELO_PRODUTO_MEDICO");

        List<String> partes = modeloSplitter.split(descricao);
        int ordem = ordensPorRegistro.getOrDefault(registro, 0);

        for (String modelo : partes) {

            lote.add(new Object[]{
                    cargaId,
                    registro,
                    ordem++,
                    AnvisaCsvReader.value(values, "NOME_TECNICO"),
                    AnvisaCsvReader.value(values, "CLASSE_RISCO"),
                    AnvisaCsvReader.value(values, "NOME_COMERCIAL"),
                    AnvisaCsvReader.value(values, "DETENTOR_REGISTRO_CADASTRO"),
                    AnvisaCsvReader.value(values, "NOME_FABRICANTE"),
                    AnvisaCsvReader.value(values, "NOME_PAIS_FABRIC"),
                    modelo,
                    AnvisaCsvReader.value(values, "DT_PUB_REGISTRO_CADASTRO"),
                    AnvisaCsvReader.value(values, "VALIDADE_REGISTRO_CADASTRO"),
                    AnvisaCsvReader.value(values, "DT_ATUALIZACAO_DADO")
            });
            count.incrementAndGet();
        }

        ordensPorRegistro.put(registro, ordem);
    }

    private void inserirModelos(List<Object[]> lote) {

        if (lote.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate("""
                insert into anvisa_modelo (
                    carga_id, numero_registro_cadastro, modelo_ordem, nome_tecnico,
                    classe_risco, nome_comercial, detentor_registro_cadastro,
                    nome_fabricante, nome_pais_fabric, ds_modelo_produto_medico,
                    dt_pub_registro_cadastro, validade_registro_cadastro, dt_atualizacao_dado
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (carga_id, numero_registro_cadastro, modelo_ordem) do update set
                    nome_tecnico = excluded.nome_tecnico,
                    classe_risco = excluded.classe_risco,
                    nome_comercial = excluded.nome_comercial,
                    detentor_registro_cadastro = excluded.detentor_registro_cadastro,
                    nome_fabricante = excluded.nome_fabricante,
                    nome_pais_fabric = excluded.nome_pais_fabric,
                    ds_modelo_produto_medico = excluded.ds_modelo_produto_medico,
                    dt_pub_registro_cadastro = excluded.dt_pub_registro_cadastro,
                    validade_registro_cadastro = excluded.validade_registro_cadastro,
                    dt_atualizacao_dado = excluded.dt_atualizacao_dado
                """, lote);
    }
}
