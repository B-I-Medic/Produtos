package com.medic.Web.dto.empresa;

import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.model.empresa.Viman;

import java.util.UUID;

public record EmpresaResponseDTO(

        UUID id,
        String descricao,
        MunicipioResponseDTO municipio,
        Viman viman,
        String codigoEmpresa,
        boolean possuiEstoqueInterno,
        boolean possuiEstoqueSegregado,
        boolean possuiVp
) {
}
