package com.medic.etl.service.schedule.job;

import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.model.processamento.ProcessamentoDisparo;
import com.medic.etl.model.processamento.ProcessamentoEntidade;
import com.medic.etl.model.processamento.ProcessamentoStatus;
import com.medic.etl.model.schedule.ScheduleJob;
import com.medic.etl.repository.estoque.AtualizarViewMaterializadaRepository;
import com.medic.etl.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.etl.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.etl.service.estoque.valePermanente.ProcessarValePermanenteService;
import com.medic.etl.service.processamento.ControlarProcessamentoService;
import com.medic.etl.service.schedule.RegistrarExecucaoScheduleService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class AtualizarEstoqueJob implements Job {

    private final ReentrantLock lock = new ReentrantLock();

    private final AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository;

    private final ControlarProcessamentoService processamentoService;
    private final ProcessarEstoqueInternoService processarEstoqueInternoService;
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService;
    private final ProcessarValePermanenteService processarValePermanenteService;
    private final RegistrarExecucaoScheduleService registrarExecucaoScheduleService;


    public AtualizarEstoqueJob(AtualizarViewMaterializadaRepository atualizarViewMaterializadaRepository,
                               ProcessarEstoqueInternoService processarEstoqueInternoService,
                               ProcessarEstoqueSegregadoService processarEstoqueSegregadoService,
                               ProcessarValePermanenteService processarValePermanenteService,
                               ControlarProcessamentoService processamentoService,
                               RegistrarExecucaoScheduleService registrarExecucaoScheduleService) {
        this.atualizarViewMaterializadaRepository = atualizarViewMaterializadaRepository;
        this.processarEstoqueInternoService = processarEstoqueInternoService;
        this.processarEstoqueSegregadoService = processarEstoqueSegregadoService;
        this.processarValePermanenteService = processarValePermanenteService;
        this.processamentoService = processamentoService;
        this.registrarExecucaoScheduleService = registrarExecucaoScheduleService;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.ATUALIZAR_ESTOQUE;
    }

    
    @Override
    public void run() {

        if (!lock.tryLock()) {

            processamentoService.abortarProcessamento(ProcessamentoEntidade.ESTOQUE, ProcessamentoDisparo.AUTOMATICO);
            log.info("Processamento de estoque ja esta em execucao. Nova execucao abortada");

            return;
        }

        Processamento processamento = null;

        try {

            registrarExecucaoScheduleService.registrarInicio(getJob());

            processamento = processamentoService.iniciarProcessamento(
                    ProcessamentoEntidade.ESTOQUE,
                    ProcessamentoDisparo.AUTOMATICO
            );

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

            lock.unlock();
        }
    }

}
