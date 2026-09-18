package com.medic.ETL.dto.anvisa;

import com.medic.ETL.model.processamento.Processamento;

import java.util.UUID;

public record AnvisaAtualizacaoClaim(
        UUID atualizacaoId,
        UUID leaseToken,
        Processamento processamento
) {
}
