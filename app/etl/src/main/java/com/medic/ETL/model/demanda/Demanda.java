package com.medic.ETL.model.demanda;

import lombok.Data;

import java.util.UUID;

@Data
public class Demanda {

    private UUID processamento;
    private String codEmpresa;
    private String ibge;
    private String codProduto;
    private int qntOrcado;
    private int qntAprovado;
    private int qntAgendado;
    private int qntUtilizado;
    private int qntTotal;

}
