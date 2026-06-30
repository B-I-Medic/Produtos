package com.medic.Web.dto.empresa;

import java.util.UUID;

public record EmpresaMunicipioResponseDTO(

        UUID id,
        UUID idEmpresa,
        UUID idMunicipio
) {
}
