package com.medic.Web.dto.necessidade;

import java.util.UUID;

public record NecessidadeResponseDTO(

        UUID id,
        UUID idEmpresaMunicipio,
        String codProduto,
        int estoque,
        int demanda,
        int necessidade
) {
}
