package com.medic.Web.dto.config.estoque.interno;

import java.util.UUID;

public record EstoqueInternoResponseDTO(

        UUID id,
        String centro_distribuicao,
        String empresa,
        String municipio,
        String estado
) {
}
