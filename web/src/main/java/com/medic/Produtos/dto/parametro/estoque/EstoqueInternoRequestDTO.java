package com.medic.Produtos.dto.parametro.estoque;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueInternoRequestDTO(

        @NotNull(message = "O id da empresa e obrigatorio")
        UUID idEmpresa,

        @NotNull(message = "O id do subCd e obrigatorio")
        UUID comporSubCd
) {
}
