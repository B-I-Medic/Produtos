package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.processamento.ProcessamentoCustomRepository;
import com.medic.ETL.service.demanda.ProcessarDemandaService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AtualizarDemandaJob implements Job {

    private static final long DEMANDA_LOCK_KEY = 872343L;

    private final ProcessarDemandaService processarDemandaService;
    private final ProcessamentoCustomRepository processamentoRepository;
    private final ControlarProcessamentoService processamentoService;

    public AtualizarDemandaJob(ProcessarDemandaService processarDemandaService,
                               ProcessamentoCustomRepository processamentoRepository,
                               ControlarProcessamentoService processamentoService) {
        this.processarDemandaService = processarDemandaService;
        this.processamentoRepository = processamentoRepository;
        this.processamentoService = processamentoService;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.ATUALIZAR_DEMANDA;
    }

    @Override
    public void run() {

        if (processamentoRepository.lockEmUso(DEMANDA_LOCK_KEY)) {

            processamentoService.abortarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO);
            log.info("Processamento de demanda ja esta em execucao. Nova execucao abortada");

            return;
        }

        Processamento processamento = processamentoService.iniciarProcessamento(ProcessamentoEntidade.DEMANDA, ProcessamentoDisparo.AUTOMATICO);

        try {

            processarDemandaService.atualizarDemanda(processamento);
            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar a atualizacao de demanda.", exception);
            throw exception;

        } finally {

            processamentoRepository.liberarLock(DEMANDA_LOCK_KEY);
        }
    }
}

