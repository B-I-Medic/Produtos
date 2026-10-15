package com.medic.web.dto.config.estoque.segregado;

import com.medic.web.dto.empresa.EmpresaResponseDTO;
import com.medic.web.dto.municipio.MunicipioResponseDTO;

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
