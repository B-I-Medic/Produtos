package com.medic.web.dto.anvisa;

import java.util.List;

public record AnvisaEmpresaComparacaoDTO(
        String descricao,
        String viman,
        List<AnvisaProdutoComparacaoDTO> produtos
) {
}
