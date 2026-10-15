package com.medic.web.dto.cd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medic.web.dto.municipio.MunicipioResponseDTO;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CentroDistribuicaoResponseDTO(

        UUID id,
        String descricao,
        MunicipioResponseDTO municipio
) {
}
