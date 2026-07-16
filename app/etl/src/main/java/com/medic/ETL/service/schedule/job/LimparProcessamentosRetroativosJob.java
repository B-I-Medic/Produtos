package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.processamento.ProcessamentoCustomRepositoryImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LimparProcessamentosRetroativosJob implements Job {

    private final ProcessamentoCustomRepositoryImpl processamentoRepository;

    public LimparProcessamentosRetroativosJob(ProcessamentoCustomRepositoryImpl processamentoRepository) {
        this.processamentoRepository = processamentoRepository;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.EXCLUIR_PROCESSAMENTOS_ANTIGOS;
    }

    
    @Override
    public void run() {

        log.info("Iniciando limpeza de processamentos retroativos");
        processamentoRepository.excluirProcessamentosAntigos();
    }
}
