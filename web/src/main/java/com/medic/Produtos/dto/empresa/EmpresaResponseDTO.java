package com.medic.Produtos.dto.empresa;

import com.medic.Produtos.model.empresa.Viman;

import java.util.UUID;

public record EmpresaResponseDTO(

        UUID id,
        String descricao,
        Viman viman,
        String codigoEmpresa,
        boolean possuiEstoqueInterno,
        boolean possuiEstoqueSegregado,
        boolean possuiVp
) {
}
