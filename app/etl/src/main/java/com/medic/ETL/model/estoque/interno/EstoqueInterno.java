package com.medic.ETL.model.estoque.interno;

import lombok.Data;

import java.util.UUID;

@Data
public class EstoqueInterno {

    private UUID processamento;
    private String viman;
    private String codEmpresa;
    private UUID idEmpresaMunicipio;
    private String codProduto;
    private Integer qntDisponivel;
}
