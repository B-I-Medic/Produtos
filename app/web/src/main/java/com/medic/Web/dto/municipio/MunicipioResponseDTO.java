package com.medic.Web.dto.municipio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medic.Web.dto.cd.CentroDistribuicaoResponseDTO;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MunicipioResponseDTO(

        UUID id,
        String descricao,
        String codigoIbge,
        String estado,
        CentroDistribuicaoResponseDTO centroDistribuicao
) {

    public MunicipioResponseDTO(UUID id,
                                String descricao,
                                String codigoIbge,
                                String estado) {
        this(id, descricao, codigoIbge, estado, null);
    }
}
