package com.medic.ETL.repository;

import com.medic.ETL.model.demanda.Demanda;
import com.medic.ETL.model.estoque.interno.EstoqueInterno;
import com.medic.ETL.model.estoque.segregado.EstoqueSegregado;
import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import com.medic.ETL.repository.demanda.ConsultaDemandaRepository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoS00Repository;
import com.medic.ETL.repository.estoque.interno.ConsultaEstoqueInternoUFXRepository;
import com.medic.ETL.repository.estoque.segregado.ConsultaConsultaSegregadoUFXRepository;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaS00Repository;
import com.medic.ETL.repository.estoque.valePermanente.ConsultaUFXRepository;
import com.medic.ETL.repository.parametro.EstoqueInternoParametroRepository;
import com.medic.ETL.repository.parametro.EstoqueSegregadoParametroRepository;
import com.medic.ETL.repository.parametro.ValePermanenteParametroRepository;
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
        UUID empresaMunicipio = UUID.randomUUID();

        JdbcTemplate s00 = mock(JdbcTemplate.class);
        ResultSet s00ResultSet = stockResultSet(processamento, empresaMunicipio);
        stubQuery(s00, s00ResultSet);
        ValePermanente s00Vale = new ConsultaS00Repository(s00).consultar("select").getFirst();

        JdbcTemplate ufxValeTemplate = mock(JdbcTemplate.class);
        ResultSet ufxValeResultSet = stockResultSet(processamento, empresaMunicipio);
        stubQuery(ufxValeTemplate, ufxValeResultSet);
        ValePermanente ufxVale = new ConsultaUFXRepository(ufxValeTemplate).consultar("select").getFirst();

        JdbcTemplate segregadoTemplate = mock(JdbcTemplate.class);
        ResultSet segregadoResultSet = stockResultSet(processamento, empresaMunicipio);
        stubQuery(segregadoTemplate, segregadoResultSet);
        EstoqueSegregado segregado = new ConsultaConsultaSegregadoUFXRepository(segregadoTemplate)
                .consultar("select")
                .getFirst();

        JdbcTemplate ufxInternoTemplate = mock(JdbcTemplate.class);
        ResultSet ufxInternoResultSet = stockResultSet(processamento, empresaMunicipio);
        stubQuery(ufxInternoTemplate, ufxInternoResultSet);
        EstoqueInterno ufxInterno = new ConsultaEstoqueInternoUFXRepository(ufxInternoTemplate)
                .consultar("select")
                .getFirst();

        JdbcTemplate s00InternoTemplate = mock(JdbcTemplate.class);
        ResultSet s00InternoResultSet = stockResultSet(processamento, empresaMunicipio);
        stubQuery(s00InternoTemplate, s00InternoResultSet);
        EstoqueInterno s00Interno = new ConsultaEstoqueInternoS00Repository(s00InternoTemplate)
                .consultar("select")
                .getFirst();

        assertEquals(processamento, s00Vale.getProcessamento());
        assertEquals(empresaMunicipio, ufxVale.getIdEmpresaMunicipio());
        assertEquals("UFX", segregado.getViman());
        assertEquals("001", ufxInterno.getCodEmpresa());
        assertEquals(5, s00Interno.getQntDisponivel());
    }

    @Test
    void shouldMapAllParameterRepositories() throws Exception {

        UUID empresaMunicipio = UUID.randomUUID();

        JdbcTemplate internoTemplate = mock(JdbcTemplate.class);
        ResultSet internoResultSet = mock(ResultSet.class);
        when(internoResultSet.getObject("id_empresa_municipio", UUID.class)).thenReturn(empresaMunicipio);
        when(internoResultSet.getString("viman")).thenReturn("S00");
        when(internoResultSet.getString("codEmpresa")).thenReturn("001");
        stubQuery(internoTemplate, internoResultSet);

        JdbcTemplate segregadoTemplate = mock(JdbcTemplate.class);
        ResultSet segregadoResultSet = mock(ResultSet.class);
        when(segregadoResultSet.getObject("id_empresa_municipio", UUID.class)).thenReturn(empresaMunicipio);
        when(segregadoResultSet.getString("codSegregado")).thenReturn("10");
        when(segregadoResultSet.getString("viman")).thenReturn("UFX");
        when(segregadoResultSet.getString("codEmpresa")).thenReturn("002");
        stubQuery(segregadoTemplate, segregadoResultSet);

        JdbcTemplate valeTemplate = mock(JdbcTemplate.class);
        ResultSet valeResultSet = mock(ResultSet.class);
        when(valeResultSet.getObject("id_empresa_municipio", UUID.class)).thenReturn(empresaMunicipio);
        when(valeResultSet.getString("codVp")).thenReturn("20");
        when(valeResultSet.getString("viman")).thenReturn("S00");
        when(valeResultSet.getString("codEmpresa")).thenReturn("003");
        stubQuery(valeTemplate, valeResultSet);

        var interno = new EstoqueInternoParametroRepository(internoTemplate)
                .obterEstoqueInternoParametros().getFirst();
        var segregado = new EstoqueSegregadoParametroRepository(segregadoTemplate)
                .obterEstoqueSegregadoParametros().getFirst();
        var vale = new ValePermanenteParametroRepository(valeTemplate)
                .obterValePermanenteParametros().getFirst();

        assertEquals(empresaMunicipio, interno.getIdEmpresaMunicipio());
        assertEquals("10", segregado.getCodSegregado());
        assertEquals("20", vale.getCodVp());
    }

    private ResultSet stockResultSet(UUID processamento, UUID empresaMunicipio) throws Exception {

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("Processamento")).thenReturn(processamento.toString());
        when(resultSet.getString("Viman")).thenReturn("UFX");
        when(resultSet.getString("CodEmpresa")).thenReturn("001");
        when(resultSet.getString("IdEmpresaMunicipio")).thenReturn(empresaMunicipio.toString());
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
