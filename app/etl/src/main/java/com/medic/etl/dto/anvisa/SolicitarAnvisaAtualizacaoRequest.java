package com.medic.etl.dto.anvisa;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SolicitarAnvisaAtualizacaoRequest(

        @NotNull UUID solicitadoPor
) {
}
