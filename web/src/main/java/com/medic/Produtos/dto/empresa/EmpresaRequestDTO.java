package com.medic.Produtos.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.medic.Produtos.model.empresa.Viman;

public record EmpresaRequestDTO(

        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @NotNull(message = "O viman e obrigatorio")
        Viman viman,

        @NotBlank(message = "O codigo viman e obrigatorio")
        String codigoEmpresa,

        boolean possuiEstoqueInterno,
        boolean possuiEstoqueSegregado,
        boolean possuiVp
) {
}
