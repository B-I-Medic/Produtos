package com.medic.Web.dto.config.estoque.segregado;

public record EstoqueSegregadoFilterDTO(

        Integer cod_segregado,
        String empresa,
        String municipio,
        String estado
) {
}
