package com.medic.Produtos.dto.parametro.estoque;

import java.util.UUID;

public record ValePermanenteResponseDTO(

        UUID id,
        UUID idEmpresa,
        int codVp,
        UUID comporSubCd
) {
}
