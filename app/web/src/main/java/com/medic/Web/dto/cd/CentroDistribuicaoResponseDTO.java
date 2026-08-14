package com.medic.Web.dto.cd;

import com.medic.Web.dto.municipio.MunicipioResumoResponseDTO;

import java.util.UUID;

public record CentroDistribuicaoResponseDTO(

        UUID id,
        String descricao,
        MunicipioResumoResponseDTO municipio
) {
}
