package com.medic.Web.dto.empresa;

import java.util.UUID;

public record EmpresaMunicipioResponseDTO(

        UUID id,
        String empresa,
        String municipio,
        String estado,
        String centroDistribuicao
) {
}
