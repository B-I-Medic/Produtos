package com.medic.Produtos.dto.municipio;

import jakarta.validation.constraints.NotBlank;

public record MunicipioRequestDTO(

        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @NotBlank(message = "O codigo IBGE e obrigatorio")
        String codigoIbge,

        @NotBlank(message = "O estado e obrigatorio")
        String estado
) {
}
