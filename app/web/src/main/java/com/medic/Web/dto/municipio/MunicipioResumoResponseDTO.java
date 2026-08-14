package com.medic.Web.dto.municipio;

import java.util.UUID;

public record MunicipioResumoResponseDTO(

        UUID id,
        String descricao,
        String estado
) {
}
