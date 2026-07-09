package com.medic.ETL.repository.processamento;

public interface ProcessamentoCustomRepository {

    boolean lockEmUso(long lockKey);

    void liberarLock(long lockKey);
}
