package com.medic.ETL.repository;

import com.medic.ETL.model.demanda.Demanda;
import com.medic.ETL.model.estoque.interno.EstoqueInterno;
import com.medic.ETL.model.estoque.segregado.EstoqueSegregado;
import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import com.medic.ETL.repository.demanda.ConsultaDemandaRepository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoS00Repository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoUFXRepository;
import com.medic.ETL.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.ETL.repository.estoque.segregado.ConsultaConsultaSegregadoS00Repository;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaS00Repository;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaUFXRepository;
import com.medic.ETL.repository.catalogo.EstoqueSegregadoCatalogoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LegacyJdbcRepositoryCoverageTest {

    @Test
    void shouldMapDemandRows() throws Exception {

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ResultSet resultSet = mock(ResultSet.class);
        UUID processamento = UUID.randomUUID();

        when(resultSet.getString("processamento")).thenReturn(processamento.toString());
        when(resultSet.getString("CodEmpresa")).thenReturn("001");
        when(resultSet.getString("IBGE")).thenReturn("3550308");
        when(resultSet.getString("CodProduto")).thenReturn("P1");
        when(resultSet.getInt("QntOrcado")).thenReturn(1);
        when(resultSet.getInt("QntAprovado")).thenReturn(2);
        when(resultSet.getInt("QntAgendado")).thenReturn(3);
        when(resultSet.getInt("QntUtilizado")).thenReturn(4);
        when(resultSet.getInt("QntTotal")).thenReturn(10);
        stubQuery(jdbcTemplate, resultSet);

        Demanda demanda = new ConsultaDemandaRepository(jdbcTemplate).consultarUFX("select").getFirst();

        assertEquals(processamento, demanda.getProcessamento());
        assertEquals("001", demanda.getCodEmpresa());
        assertEquals("3550308", demanda.getIbge());
        assertEquals("P1", demanda.getCodProduto());
        assertEquals(10, demanda.getQntTotal());
    }

    @Test
    void shouldMapStockRowsFromAllSources() throws Exception {

        UUID processamento = UUID.randomUUID();

        JdbcTemplate s00 = mock(JdbcTemplate.class);
        ResultSet s00ResultSet = stockResultSet(processamento);
        stubQuery(s00, s00ResultSet);
        ValePermanente s00Vale = new ConsultaS00Repository(s00).consultar("select").getFirst();

        JdbcTemplate ufxValeTemplate = mock(JdbcTemplate.class);
        ResultSet ufxValeResultSet = stockResultSet(processamento);
        stubQuery(ufxValeTemplate, ufxValeResultSet);
        ValePermanente ufxVale = new ConsultaUFXRepository(ufxValeTemplate).consultar("select").getFirst();

        JdbcTemplate segregadoTemplate = mock(JdbcTemplate.class);
        ResultSet segregadoResultSet = stockResultSet(processamento);
        stubQuery(segregadoTemplate, segregadoResultSet);
        EstoqueSegregado segregado = new ConsultaConsultaSegregadoUFXRepository(segregadoTemplate)
                .consultar("select")
                .getFirst();

        JdbcTemplate segregadoS00Template = mock(JdbcTemplate.class);
        ResultSet segregadoS00ResultSet = stockResultSet(processamento);
        stubQuery(segregadoS00Template, segregadoS00ResultSet);
        EstoqueSegregado segregadoS00 = new ConsultaConsultaSegregadoS00Repository(segregadoS00Template)
                .consultar("select")
                .getFirst();

        JdbcTemplate ufxInternoTemplate = mock(JdbcTemplate.class);
        ResultSet ufxInternoResultSet = stockResultSet(processamento);
        stubQuery(ufxInternoTemplate, ufxInternoResultSet);
        EstoqueInterno ufxInterno = new ConsultaEstoqueInternoUFXRepository(ufxInternoTemplate)
                .consultar("select")
                .getFirst();

        JdbcTemplate s00InternoTemplate = mock(JdbcTemplate.class);
        ResultSet s00InternoResultSet = stockResultSet(processamento);
        stubQuery(s00InternoTemplate, s00InternoResultSet);
        EstoqueInterno s00Interno = new ConsultaEstoqueInternoS00Repository(s00InternoTemplate)
                .consultar("select")
                .getFirst();

        assertEquals(processamento, s00Vale.getProcessamento());
        assertEquals("3550308", ufxVale.getCodIBGE());
        assertEquals("3550308", segregado.getCodIBGE());
        assertEquals("UFX", segregadoS00.getViman());
        assertEquals("P1", segregadoS00.getCodProduto());
        assertEquals(5, segregadoS00.getQntDisponivel());
        assertEquals("001", ufxInterno.getCodEmpresa());
        assertEquals(5, s00Interno.getQntDisponivel());
    }

    @Test
    void shouldMapSegregatedParameterRepository() throws Exception {

        JdbcTemplate segregadoTemplate = mock(JdbcTemplate.class);
        ResultSet segregadoResultSet = mock(ResultSet.class);
        when(segregadoResultSet.getString("codEmpresa")).thenReturn("002");
        when(segregadoResultSet.getString("CodIBGE")).thenReturn("3550308");
        when(segregadoResultSet.getString("codSegregado")).thenReturn("10");
        stubQuery(segregadoTemplate, segregadoResultSet);
        var segregado = new EstoqueSegregadoCatalogoRepository(segregadoTemplate)
                .obterEstoqueSegregadoCatalogo().getFirst();

        assertEquals("002", segregado.getCodEmpresa());
        assertEquals("3550308", segregado.getCodIBGE());
        assertEquals("10", segregado.getCodSegregado());
    }

    private ResultSet stockResultSet(UUID processamento) throws Exception {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("Processamento")).thenReturn(processamento.toString());
        when(resultSet.getString("Viman")).thenReturn("UFX");
        when(resultSet.getString("CodEmpresa")).thenReturn("001");
        when(resultSet.getString("CodIBGE")).thenReturn("3550308");
        when(resultSet.getString("CodProduto")).thenReturn("P1");
        when(resultSet.getInt("QntDisponivel")).thenReturn(5);
        return resultSet;
    }

    @SuppressWarnings("unchecked")
    private void stubQuery(JdbcTemplate jdbcTemplate, ResultSet resultSet) {

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<?> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });
    }
}
