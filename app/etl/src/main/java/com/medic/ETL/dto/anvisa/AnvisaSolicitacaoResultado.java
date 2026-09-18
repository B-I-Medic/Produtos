package com.medic.ETL.dto.anvisa;

public record AnvisaSolicitacaoResultado(
        AnvisaAtualizacaoResponse response,
        Tipo tipo
) {

    public enum Tipo {
        ACEITA,
        CONCLUIDA,
        COOLDOWN
    }
}
