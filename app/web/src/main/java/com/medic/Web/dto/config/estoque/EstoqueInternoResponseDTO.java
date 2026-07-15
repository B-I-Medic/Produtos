package com.medic.Web.dto.config.estoque;

import java.util.UUID;

public record EstoqueInternoResponseDTO(

        UUID id,
        UUID idEmpresa,
        UUID id_empresa_municipio
) {
}
