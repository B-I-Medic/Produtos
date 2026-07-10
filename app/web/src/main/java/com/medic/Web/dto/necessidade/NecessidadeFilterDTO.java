package com.medic.Web.dto.necessidade;

import com.medic.Web.model.necessidade.AgrupamentosPadrao;

import java.util.List;

public record NecessidadeFilterDTO(

        String centroDistribuicao,
        String empresa,
        String municipio,
        String produto,
        String marca,
        List<AgrupamentosPadrao> groupBy
) {
}
