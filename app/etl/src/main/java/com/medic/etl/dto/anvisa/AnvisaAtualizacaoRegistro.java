package com.medic.etl.dto.anvisa;

import com.medic.etl.model.anvisa.AnvisaAtualizacaoStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AnvisaAtualizacaoRegistro(
        UUID id,
        LocalDate dataReferencia,
        AnvisaAtualizacaoStatus status,
        int tentativas,
        UUID solicitadoPor,
        Instant solicitadoEm,
        Instant iniciadoEm,
        Instant concluidoEm,
        Instant ultimaFalhaEm,
        String ultimoErro,
        UUID processamentoId,
        UUID leaseToken,
        Instant leaseExpiraEm
) {
}
