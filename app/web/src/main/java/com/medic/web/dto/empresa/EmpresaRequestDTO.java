package com.medic.web.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.medic.web.model.empresa.Viman;

import java.util.UUID;

public record EmpresaRequestDTO(

        @NotBlank(message = "A descricao e obrigatoria")
        String descricao,

        @NotNull(message = "O id do municipio e obrigatorio")
        UUID municipioId,

        @NotNull(message = "O viman e obrigatorio")
        Viman viman,

        @NotBlank(message = "O codigo viman e obrigatorio")
        String codigoEmpresa,

        boolean possuiEstoqueInterno,
        boolean possuiEstoqueSegregado,
        boolean possuiVp
) {
}
