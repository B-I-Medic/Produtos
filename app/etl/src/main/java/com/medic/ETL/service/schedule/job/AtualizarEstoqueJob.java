package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.estoque.AtualizarViewMaterializadaRepository;
import com.medic.ETL.repository.processamento.ProcessamentoRepository;
import com.medic.ETL.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.ETL.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.ETL.service.estoque.valePermanente.ProcessarValePermanenteService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AtualizarEstoqueJob implements Job {

    private static final long ESTOQUE_LOCK_KEY = 872341L;


    private final ProcessamentoRepository processamentoRepository;
    private final AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository;

    private final ControlarProcessamentoService processamentoService;
    private final ProcessarEstoqueInternoService processarEstoqueInternoService;
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService;
    private final ProcessarValePermanenteService processarValePermanenteService;


    public AtualizarEstoqueJob(AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository,
                               ProcessarEstoqueInternoService processarEstoqueInternoService,
                               ProcessarEstoqueSegregadoService processarEstoqueSegregadoService,
                               ProcessarValePermanenteService processarValePermanenteService,
                               ControlarProcessamentoService processamentoService,
                               ProcessamentoRepository processamentoRepository) {
        this.atualizarViewMaterializadaRepository = atualizarViewMaterializadaRepository;
        this.processarEstoqueInternoService = processarEstoqueInternoService;
        this.processarEstoqueSegregadoService = processarEstoqueSegregadoService;
        this.processarValePermanenteService = processarValePermanenteService;
        this.processamentoService = processamentoService;
        this.processamentoRepository = processamentoRepository;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.ATUALIZAR_ESTOQUE;
    }

    @PostConstruct
    public void run() {

        if (processamentoRepository.lockEmUso(ESTOQUE_LOCK_KEY)) {

            processamentoService.abortarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);
            log.info("Processamento de estoque ja esta em execucao. Nova execucao abortada");

            return;
        }

        Processamento processamento = processamentoService.iniciarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);

        try {

            processarEstoqueInternoService.processarEstoqueInterno(processamento);
            processarEstoqueSegregadoService.processarEstoqueSegregado(processamento);
            processarValePermanenteService.processarValePermanente(processamento);

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

            atualizarViewMaterializadaRepository.atualizar();

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar atualização de estoque", exception);
            throw exception;

        } finally {

            processamentoRepository.liberarLock(ESTOQUE_LOCK_KEY);
        }
    }

}
