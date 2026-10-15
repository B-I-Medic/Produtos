package com.medic.web.dto.processamento;

import com.medic.web.model.processamento.ProcessamentoDisparo;
import com.medic.web.model.processamento.ProcessamentoEntidade;
import com.medic.web.model.processamento.ProcessamentoStatus;

import java.time.Instant;
import java.util.UUID;

public record ProcessamentoResponseDTO(

        ProcessamentoEntidade entidade,
        UUID id,
        ProcessamentoStatus status,
        ProcessamentoDisparo tipoDisparo,
        Instant iniciadoEm,
        Instant concluidoEm

) {
}
