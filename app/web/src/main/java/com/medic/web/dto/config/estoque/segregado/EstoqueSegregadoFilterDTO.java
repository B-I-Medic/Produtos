package com.medic.web.dto.config.estoque.segregado;

public record EstoqueSegregadoFilterDTO(

        Integer cod_segregado,
        String empresa,
        String municipio,
        String estado
) {
}
