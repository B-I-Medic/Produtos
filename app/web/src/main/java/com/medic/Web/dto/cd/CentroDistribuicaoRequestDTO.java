package com.medic.Web.dto.cd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CentroDistribuicaoRequestDTO(

        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @NotNull(message = "O id do municipio e obrigatorio")
        UUID municipioId
) {
}
