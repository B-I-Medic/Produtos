package com.medic.Web.dto.config.estoque.segregado;

import java.util.UUID;

public record EstoqueSegregadoResponseDTO(

        UUID id,
        UUID idEmpresa,
        int codSegregado,
        UUID id_empresa_municipio
) {
}
