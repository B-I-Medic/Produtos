package com.medic.Produtos.dto.parametro.taxa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TaxaRequestDTO(

        @NotNull(message = "O valor da taxa é obrigatório")
        @Min(value = 0, message = "A taxa não pode ser menor do que 0")
        BigDecimal taxa
) {
}
