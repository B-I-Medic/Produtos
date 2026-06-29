package com.medic.Web.dto.parametro.taxa;

import com.medic.Web.model.parametro.taxa.TaxaEnum;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxaResponseDTO(

        UUID id,
        TaxaEnum descricao,
        BigDecimal taxa
) {
}
