package com.medic.Web.dto.parametro.estoque;

import java.util.UUID;

public record EstoqueSegregadoResponseDTO(

        UUID id,
        UUID idEmpresa,
        int codSegregado,
        UUID comporSubCd
) {
}
