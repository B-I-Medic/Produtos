package com.medic.Web.dto.empresa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;
import com.medic.Web.model.empresa.Viman;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmpresaResponseDTO(

        UUID id,
        String descricao,
        MunicipioResponseDTO municipio,
        Viman viman,
        String codigoEmpresa,
        Boolean possuiEstoqueInterno,
        Boolean possuiEstoqueSegregado,
        Boolean possuiVp
) {
}
