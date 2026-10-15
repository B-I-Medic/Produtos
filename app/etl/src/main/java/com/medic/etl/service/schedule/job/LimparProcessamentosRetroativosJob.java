package com.medic.etl.service.schedule.job;

import com.medic.etl.model.schedule.ScheduleJob;
import com.medic.etl.repository.processamento.ProcessamentoCustomRepositoryImpl;
import com.medic.etl.service.schedule.RegistrarExecucaoScheduleService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LimparProcessamentosRetroativosJob implements Job {

    private final ProcessamentoCustomRepositoryImpl processamentoRepository;
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService;

    public LimparProcessamentosRetroativosJob(ProcessamentoCustomRepositoryImpl processamentoRepository,
                                              RegistrarExecucaoScheduleService registrarExecucaoScheduleService) {
        this.processamentoRepository = processamentoRepository;
        this.registrarExecucaoScheduleService = registrarExecucaoScheduleService;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.EXCLUIR_PROCESSAMENTOS_ANTIGOS;
    }

    
    @Override
    public void run() {

        registrarExecucaoScheduleService.registrarInicio(getJob());
        log.info("Iniciando limpeza de processamentos retroativos");
        processamentoRepository.excluirProcessamentosAntigos();
    }
}
