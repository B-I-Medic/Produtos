package com.medic.Web.dto.processamento;

import com.medic.Web.model.processamento.ProcessamentoDisparo;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.model.processamento.ProcessamentoStatus;

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
