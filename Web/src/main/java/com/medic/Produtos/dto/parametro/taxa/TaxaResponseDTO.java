package com.medic.Produtos.dto.parametro.taxa;

import com.medic.Produtos.model.parametro.taxa.TaxaEnum;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxaResponseDTO(

        UUID id,
        TaxaEnum descricao,
        BigDecimal taxa
) {
}
