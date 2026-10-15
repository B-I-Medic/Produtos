package com.medic.web.dto.anvisa;

import java.util.List;

public record AnvisaResponseDTO(
        AnvisaProdutoDTO produto,
        List<AnvisaModeloDTO> modelos
) {
}
