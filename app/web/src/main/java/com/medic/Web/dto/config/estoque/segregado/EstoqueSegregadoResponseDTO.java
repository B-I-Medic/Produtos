package com.medic.Web.dto.config.estoque.segregado;

import com.medic.Web.dto.empresa.EmpresaResponseDTO;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;

import java.util.UUID;

public record EstoqueSegregadoResponseDTO(

        UUID id,
        int codSegregado,
        String descricao,
        boolean ativo,
        EmpresaResponseDTO empresa,
        MunicipioResponseDTO municipio
) {
}
