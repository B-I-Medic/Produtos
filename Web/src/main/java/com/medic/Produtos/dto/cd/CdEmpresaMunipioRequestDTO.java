package com.medic.Produtos.dto.cd;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CdEmpresaMunipioRequestDTO(

        @NotNull(message = "O id do centro de distribuição é obrigatório")
        UUID idCd,

        @NotNull(message = "O id da empresa-municipio é obrigatório")
        UUID idEmpresaMunicipio
) {
}
