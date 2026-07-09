package com.medic.Web.dto.necessidade;

public record NecessidadeAgrupadoPorCDResponseDTO(

        String centroDistribuicao,
        String codigoProduto,
        String produto,
        String marca,
        String anvisa,
        int qntEstoqueInterno,
        int qntEstoqueSegregado,
        int qntEstoqueVP,
        int qntEstoqueTotal,
        int qntDemandaOrcado,
        int qntDemandaAprovado,
        int qntDemandaAgendado,
        int qntDemandaUtilizado,
        int qntDemandaTotal,
        int necessidadeDeCompra
) {
}
