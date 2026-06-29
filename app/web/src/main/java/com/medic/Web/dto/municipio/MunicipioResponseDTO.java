package com.medic.Web.dto.municipio;

import java.util.UUID;

public record MunicipioResponseDTO(

        UUID id,
        String descricao,
        String codigoIbge,
        String estado
) {
}
