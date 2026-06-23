package com.medic.Produtos.dto.parametro.estoque;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueSegregadoRequestDTO(

        @NotNull(message = "O id da empresa e obrigatorio")
        UUID idEmpresa,

        @Min(value = 0, message = "O codigo segregado nao pode ser negativo")
        int codSegregado,

        @NotNull(message = "O id do subCd e obrigatorio")
        UUID comporSubCd
) {
}
