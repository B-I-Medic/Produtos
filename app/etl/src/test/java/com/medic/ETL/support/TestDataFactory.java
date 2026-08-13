package com.medic.ETL.support;

import com.medic.ETL.dto.parametro.EstoqueInternoParametroDTO;
import com.medic.ETL.dto.parametro.EstoqueSegregadoParametroDTO;
import com.medic.ETL.dto.parametro.ValePermanenteParametroDTO;
import com.medic.ETL.model.demanda.Demanda;
import com.medic.ETL.model.empresa.EmpresaModel;
import com.medic.ETL.model.estoque.interno.EstoqueInterno;
import com.medic.ETL.model.estoque.segregado.EstoqueSegregado;
import com.medic.ETL.model.estoque.valePermanente.ValePermanente;
import com.medic.ETL.model.periodo.PeriodoModel;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.produto.Produto;

import java.time.Instant;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Processamento processamento() {
        Processamento processamento = new Processamento();
        processamento.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        return processamento;
    }

    public static EmpresaModel empresa(String codigoEmpresa) {
        EmpresaModel empresa = new EmpresaModel();
        empresa.setId(UUID.randomUUID());
        empresa.setViman("UFX");
        empresa.setCodigoEmpresa(codigoEmpresa);
        empresa.setPossuiEstoqueInterno(true);
        return empresa;
    }

    public static PeriodoModel periodo(String descricao, String inicio, String fim) {
        PeriodoModel periodo = new PeriodoModel();
        periodo.setDescricao(descricao);
        periodo.setDataInicialViman(inicio);
        periodo.setDataFinalViman(fim);
        return periodo;
    }

    public static Produto produto(String viman, String codEmpresa, String codProduto) {
        Produto produto = new Produto();
        produto.setViman(viman);
        produto.setCodEmpresa(codEmpresa);
        produto.setCodProduto(codProduto);
        produto.setDescricao("Produto");
        produto.setMarca("Marca");
        produto.setTipo("Tipo");
        produto.setAnvisa(123L);
        produto.setSituacao("ATIVO");
        produto.setCriadoPor("ETL");
        produto.setCriadoEm(Instant.parse("2026-08-10T10:00:00Z"));
        return produto;
    }

    public static Demanda demanda() {
        Demanda demanda = new Demanda();
        demanda.setProcessamento(processamento().getId());
        demanda.setCodEmpresa("01");
        demanda.setIbge("3550308");
        demanda.setCodProduto("P1");
        demanda.setQntOrcado(1);
        demanda.setQntAprovado(2);
        demanda.setQntAgendado(3);
        demanda.setQntUtilizado(4);
        demanda.setQntTotal(10);
        return demanda;
    }

    public static EstoqueInterno estoqueInterno(Integer quantidade) {
        EstoqueInterno estoque = new EstoqueInterno();
        estoque.setProcessamento(processamento().getId());
        estoque.setViman("UFX");
        estoque.setCodEmpresa("01");
        estoque.setIdEmpresaMunicipio(UUID.fromString("00000000-0000-0000-0000-000000000101"));
        estoque.setCodProduto("P1");
        estoque.setQntDisponivel(quantidade);
        return estoque;
    }

    public static EstoqueSegregado estoqueSegregado(Integer quantidade) {
        EstoqueSegregado estoque = new EstoqueSegregado();
        estoque.setProcessamento(processamento().getId());
        estoque.setViman("UFX");
        estoque.setCodEmpresa("01");
        estoque.setIdEmpresaMunicipio(UUID.fromString("00000000-0000-0000-0000-000000000102"));
        estoque.setCodProduto("P1");
        estoque.setQntDisponivel(quantidade);
        return estoque;
    }

    public static ValePermanente valePermanente(Integer quantidade) {
        ValePermanente estoque = new ValePermanente();
        estoque.setProcessamento(processamento().getId());
        estoque.setViman("S00");
        estoque.setCodEmpresa("02");
        estoque.setIdEmpresaMunicipio(UUID.fromString("00000000-0000-0000-0000-000000000103"));
        estoque.setCodProduto("P1");
        estoque.setQntDisponivel(quantidade);
        return estoque;
    }

    public static EstoqueInternoParametroDTO estoqueInternoParametro(UUID empresaMunicipio, String viman, String codEmpresa) {
        EstoqueInternoParametroDTO parametro = new EstoqueInternoParametroDTO();
        parametro.setIdEmpresaMunicipio(empresaMunicipio);
        parametro.setViman(viman);
        parametro.setCodEmpresa(codEmpresa);
        return parametro;
    }

    public static EstoqueSegregadoParametroDTO estoqueSegregadoParametro(UUID empresaMunicipio, String viman, String codEmpresa, String codSegregado) {
        EstoqueSegregadoParametroDTO parametro = new EstoqueSegregadoParametroDTO();
        parametro.setIdEmpresaMunicipio(empresaMunicipio);
        parametro.setViman(viman);
        parametro.setCodEmpresa(codEmpresa);
        parametro.setCodSegregado(codSegregado);
        return parametro;
    }

    public static ValePermanenteParametroDTO valePermanenteParametro(UUID empresaMunicipio, String viman, String codEmpresa, String codVp) {
        ValePermanenteParametroDTO parametro = new ValePermanenteParametroDTO();
        parametro.setIdEmpresaMunicipio(empresaMunicipio);
        parametro.setViman(viman);
        parametro.setCodEmpresa(codEmpresa);
        parametro.setCodVp(codVp);
        return parametro;
    }
}
