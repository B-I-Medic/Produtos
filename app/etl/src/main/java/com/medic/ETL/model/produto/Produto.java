package com.medic.ETL.model.produto;

import lombok.Data;

import java.time.Instant;

@Data
public class Produto {

    private String viman;
    private String codEmpresa;
    private String codProduto;
    private String descricao;
    private String marca;
    private String tipo;
    private Long anvisa;
    private String situacao;
    private String criadoPor;
    private Instant criadoEm;
}
