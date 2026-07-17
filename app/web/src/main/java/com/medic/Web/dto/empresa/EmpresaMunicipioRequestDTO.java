package com.medic.Web.dto.empresa;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmpresaMunicipioRequestDTO(

        @NotNull(message = "O id da empresa é obrigatorio")
        UUID empresaId,

        @NotNull(message = "O id do centro de distribuicao é obrigatorio")
        UUID cdId,

        @NotNull(message = "O id do municipio é obrigatorio")
        UUID municipioId
) {
}
