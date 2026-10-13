package com.medic.Web.dto.cd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medic.Web.dto.municipio.MunicipioResponseDTO;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CentroDistribuicaoResponseDTO(

        UUID id,
        String descricao,
        MunicipioResponseDTO municipio
) {
}
