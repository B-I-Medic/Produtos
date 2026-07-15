package com.medic.Web.dto.config.taxa;

import com.medic.Web.model.config.taxa.TaxaEnum;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxaResponseDTO(

        UUID id,
        TaxaEnum descricao,
        BigDecimal taxa
) {
}
