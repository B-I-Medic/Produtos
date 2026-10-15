package com.medic.etl.dto.anvisa;

import com.medic.etl.model.processamento.Processamento;

import java.util.UUID;

public record AnvisaAtualizacaoClaim(
        UUID atualizacaoId,
        UUID leaseToken,
        Processamento processamento
) {
}
