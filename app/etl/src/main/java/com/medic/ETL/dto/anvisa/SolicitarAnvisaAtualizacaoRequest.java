package com.medic.ETL.dto.anvisa;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SolicitarAnvisaAtualizacaoRequest(

        @NotNull UUID solicitadoPor
) {
}
