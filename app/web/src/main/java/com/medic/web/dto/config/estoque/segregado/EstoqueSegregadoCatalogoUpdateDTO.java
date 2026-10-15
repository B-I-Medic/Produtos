package com.medic.web.dto.config.estoque.segregado;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EstoqueSegregadoCatalogoUpdateDTO(

        @NotNull(message = "O estado de ativacao e obrigatorio")
        Boolean ativo,

        UUID idMunicipio
) {
}
