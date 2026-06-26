package com.medic.Produtos.dto.empresa;

import java.util.UUID;

public record EmpresaMunicipioResponseDTO(

        UUID id,
        UUID idEmpresa,
        UUID idMunicipio
) {
}
