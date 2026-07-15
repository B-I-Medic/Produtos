package com.medic.Web.dto.config.estoque;

import java.util.UUID;

public record ValePermanenteResponseDTO(

        UUID id,
        UUID idEmpresa,
        int codVp,
        UUID id_empresa_municipio
) {
}
