package com.medic.Web.dto.necessidade;

import com.medic.Web.model.necessidade.AgrupamentosPadrao;

import java.util.List;

public record NecessidadeFilterDTO(

        String centroDistribuicao,
        String empresa,
        String estado,
        String municipio,
        String anvisa,
        String marca,
        String produto,
        List<AgrupamentosPadrao> groupBy
) {
}
