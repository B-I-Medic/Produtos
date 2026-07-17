package com.medic.Web.dto.config.estoque.vp;

import java.util.UUID;

public record ValePermanenteResponseDTO(

        UUID id,
        String centro_distribuicao,
        String empresa,
        int codVp,
        String municipio,
        String estado
) {
}
