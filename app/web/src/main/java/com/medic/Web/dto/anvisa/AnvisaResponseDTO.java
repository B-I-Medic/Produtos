package com.medic.Web.dto.anvisa;

import java.util.List;

public record AnvisaResponseDTO(
        AnvisaProdutoDTO produto,
        List<AnvisaModeloDTO> modelos
) {
}
