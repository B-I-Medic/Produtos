package com.medic.etl.dto.catalogo;

import java.util.UUID;

public record EstoqueSegregadoCatalogoItemDTO(
        UUID idEmpresa,
        int codSegregado,
        String descricao
) {
}
