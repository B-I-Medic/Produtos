package com.medic.ETL.model.estoque.segregado;

import lombok.Data;

import java.util.UUID;

@Data
public class EstoqueSegregado {

    private UUID processamento;
    private String viman;
    private String codEmpresa;
    private UUID idEmpresaMunicipio;
    private String codProduto;
    private Integer qntDisponivel;
}
