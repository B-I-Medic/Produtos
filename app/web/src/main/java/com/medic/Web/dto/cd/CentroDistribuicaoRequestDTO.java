package com.medic.Web.dto.cd;

import jakarta.validation.constraints.NotBlank;

public record CentroDistribuicaoRequestDTO(

        @NotBlank(message = "A descricao e obrigatoria")
        String descricao
) {
}
