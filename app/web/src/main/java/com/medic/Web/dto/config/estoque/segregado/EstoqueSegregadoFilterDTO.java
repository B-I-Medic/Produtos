package com.medic.Web.dto.config.estoque.segregado;

public record EstoqueSegregadoFilterDTO(

        String cd,
        String empresa,
        String municipio,
        String estado,
        Integer cod_segregado
) {
}
