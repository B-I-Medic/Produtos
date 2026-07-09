package com.medic.Web.dto.necessidade;

public record NecessidadeFilterDTO(

        String centroDistribuicao,
        String empresa,
        String municipio,
        String produto,
        String marca
) {
}
