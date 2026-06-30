package com.medic.Web.dto.parametro.estoque;

import java.util.UUID;

public record EstoqueInternoResponseDTO(

        UUID id,
        UUID idEmpresa,
        UUID comporSubCd
) {
}
