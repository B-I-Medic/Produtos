package com.medic.Web.dto.cd;

import java.util.UUID;

public record CdEmpresaMunicipioResponseDTO(

        UUID id,
        String empresa,
        String municipio,
        String estado
) {
}
