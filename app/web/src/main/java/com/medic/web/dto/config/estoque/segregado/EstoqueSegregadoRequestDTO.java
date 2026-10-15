package com.medic.web.dto.config.estoque.segregado;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueSegregadoRequestDTO(

        @NotNull(message = "O id da empresa é obrigatorio")
        UUID idEmpresa,

        @NotNull(message = "O codigo se segregado é obrigatório")
        @Min(value = 0, message = "O codigo segregado nao pode ser negativo")
        int codSegregado,

        @NotNull(message = "O id do municipio é obrigatorio")
        UUID idMunicipio
) {
}
