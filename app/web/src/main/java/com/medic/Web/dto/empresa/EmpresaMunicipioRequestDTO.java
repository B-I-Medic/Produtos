package com.medic.Web.dto.empresa;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmpresaMunicipioRequestDTO(

        @NotNull(message = "O id da empresa e obrigatorio")
        UUID cdId,

        @NotNull(message = "O id do municipio e obrigatorio")
        UUID municipioId
) {
}
