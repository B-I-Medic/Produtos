package com.medic.Web.dto.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForecastAgrupadoResponseDTO(

        String centroDistribuicao,
        String empresa,
        String estado,
        String municipio,
        String anvisa,
        String marca,
        String codigoProduto,
        String produto,
        Long qntEstoqueInterno,
        Long qntEstoqueSegregado,
        Long qntEstoqueVP,
        Long qntEstoqueTotal,
        Long qntDemandaOrcado,
        Long qntDemandaAprovado,
        Long qntDemandaAgendado,
        Long qntDemandaUtilizado,
        Long qntDemandaTotal,
        Long necessidadeDeCompra
) {
}
