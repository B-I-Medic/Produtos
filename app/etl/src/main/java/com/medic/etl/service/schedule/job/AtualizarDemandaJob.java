package com.medic.etl.service.schedule.job;

import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.model.processamento.ProcessamentoDisparo;
import com.medic.etl.model.processamento.ProcessamentoEntidade;
import com.medic.etl.model.processamento.ProcessamentoStatus;
import com.medic.etl.model.schedule.ScheduleJob;
import com.medic.etl.service.demanda.ProcessarDemandaService;
import com.medic.etl.service.processamento.ControlarProcessamentoService;
import com.medic.etl.service.schedule.RegistrarExecucaoScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class AtualizarDemandaJob implements Job {

    private final ReentrantLock lock = new ReentrantLock();

    private final ProcessarDemandaService processarDemandaService;
    private final ControlarProcessamentoService processamentoService;
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService;

    public AtualizarDemandaJob(ProcessarDemandaService processarDemandaService,
                               ControlarProcessamentoService processamentoService,
                               RegistrarExecucaoScheduleService registrarExecucaoScheduleService) {
        this.processarDemandaService = processarDemandaService;
        this.processamentoService = processamentoService;
        this.registrarExecucaoScheduleService = registrarExecucaoScheduleService;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.ATUALIZAR_DEMANDA;
    }

    
    @Override
    public void run() {

        if (!lock.tryLock()) {

            processamentoService.abortarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO);
            log.info("Processamento de demanda ja esta em execucao. Nova execucao abortada");

            return;
        }

        Processamento processamento = null;

        try {

            registrarExecucaoScheduleService.registrarInicio(getJob());

            processamento = processamentoService.iniciarProcessamento(
                    ProcessamentoEntidade.DEMANDA,
                    ProcessamentoDisparo.AUTOMATICO
            );

            processarDemandaService.atualizarDemanda(processamento);
            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar a atualizacao de demanda.", exception);
            throw exception;

        } finally {

            lock.unlock();
        }
    }
}

