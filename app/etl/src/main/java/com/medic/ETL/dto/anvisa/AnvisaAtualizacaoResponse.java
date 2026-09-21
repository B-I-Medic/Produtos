package com.medic.ETL.dto.anvisa;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AnvisaAtualizacaoResponse(
        UUID id,
        LocalDate dataReferencia,
        String status,
        int tentativas,
        Instant solicitadoEm,
        Instant iniciadoEm,
        Instant concluidoEm,
        Instant ultimaFalhaEm,
        Instant proximaSolicitacaoEm,
        UUID processamentoId,
        String ultimoErro
) {
}
