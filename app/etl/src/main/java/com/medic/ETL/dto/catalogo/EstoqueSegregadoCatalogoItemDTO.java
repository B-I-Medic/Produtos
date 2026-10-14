package com.medic.ETL.dto.catalogo;

import java.util.UUID;

public record EstoqueSegregadoCatalogoItemDTO(
        UUID idEmpresa,
        int codSegregado,
        String descricao
) {
}
