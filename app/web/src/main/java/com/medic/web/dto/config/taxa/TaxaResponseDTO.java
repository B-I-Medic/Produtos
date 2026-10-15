package com.medic.web.dto.config.taxa;

import com.medic.web.model.config.taxa.TaxaEnum;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxaResponseDTO(

        UUID id,
        TaxaEnum descricao,
        BigDecimal taxa
) {
}
