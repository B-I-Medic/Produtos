package com.medic.Web.dto.anvisa;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AnvisaAtualizacaoResponseDTO(

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
