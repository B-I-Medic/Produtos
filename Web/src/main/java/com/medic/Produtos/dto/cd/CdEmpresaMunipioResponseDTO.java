package com.medic.Produtos.dto.cd;

import java.util.UUID;

public record CdEmpresaMunipioResponseDTO(

        UUID id,
        UUID idCd,
        UUID idEmpresaMunicipio
) {
}
