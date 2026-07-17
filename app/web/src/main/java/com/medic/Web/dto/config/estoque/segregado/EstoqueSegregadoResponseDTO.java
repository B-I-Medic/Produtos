package com.medic.Web.dto.config.estoque.segregado;

import java.util.UUID;

public record EstoqueSegregadoResponseDTO(

        UUID id,
        String centro_distribuicao,
        String empresa,
        int codSegregado,
        String municipio,
        String estado
) {
}
