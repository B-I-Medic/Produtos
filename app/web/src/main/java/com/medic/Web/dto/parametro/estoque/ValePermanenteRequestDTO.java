package com.medic.Web.dto.parametro.estoque;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ValePermanenteRequestDTO(

        @NotNull(message = "O id da empresa e obrigatorio")
        UUID idEmpresa,

        @Min(value = 0, message = "O codigo vp nao pode ser negativo")
        int codVp,

        @NotNull(message = "O id do subCd e obrigatorio")
        UUID comporSubCd
) {
}
