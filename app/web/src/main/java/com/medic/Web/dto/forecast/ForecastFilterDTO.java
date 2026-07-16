package com.medic.Web.dto.forecast;

import java.util.List;

public record ForecastFilterDTO(

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
