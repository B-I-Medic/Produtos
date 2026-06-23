package com.medic.Produtos.dto.municipio;

import java.util.UUID;

public record MunicipioResponseDTO(

        UUID id,
        String descricao,
        String codigoIbge,
        String estado
) {
}
