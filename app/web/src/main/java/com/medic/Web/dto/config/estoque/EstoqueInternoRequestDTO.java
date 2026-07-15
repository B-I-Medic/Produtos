package com.medic.Web.dto.config.estoque;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueInternoRequestDTO(

        @NotNull(message = "O id da empresa e obrigatorio")
        UUID idEmpresa,

        @NotNull(message = "O id da empresa-municipio e obrigatorio")
        UUID id_empresa_municipio
) {
}
