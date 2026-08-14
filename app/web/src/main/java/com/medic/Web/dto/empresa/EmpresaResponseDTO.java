package com.medic.Web.dto.empresa;

import com.medic.Web.dto.municipio.MunicipioResumoResponseDTO;
import com.medic.Web.model.empresa.Viman;

import java.util.UUID;

public record EmpresaResponseDTO(

        UUID id,
        String descricao,
        MunicipioResumoResponseDTO municipio,
        Viman viman,
        String codigoEmpresa,
        boolean possuiEstoqueInterno,
        boolean possuiEstoqueSegregado,
        boolean possuiVp
) {
}
