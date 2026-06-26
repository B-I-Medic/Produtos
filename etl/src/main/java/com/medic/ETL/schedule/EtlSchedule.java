package com.medic.ETL.schedule;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.processamento.ProcessamentoLockRepository;
import com.medic.ETL.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.ETL.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.ETL.service.estoque.valePermanente.ProcessarValePermanenteService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EtlSchedule {

    private static final long PROCESSAMENTO_LOCK_KEY = 872341L;

    private final ControlarProcessamentoService processamentoService;
    private final ProcessamentoLockRepository processamentoLockRepository;
    private final ProcessarEstoqueInternoService processarEstoqueInternoService;
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService;
    private final ProcessarValePermanenteService processarValePermanenteService;

    public EtlSchedule(ProcessarEstoqueInternoService processarEstoqueInternoService,
                       ProcessarEstoqueSegregadoService processarEstoqueSegregadoService,
                       ProcessarValePermanenteService processarValePermanenteService,
                       ControlarProcessamentoService processamentoService,
                       ProcessamentoLockRepository processamentoLockRepository) {
        this.processarEstoqueInternoService = processarEstoqueInternoService;
        this.processarEstoqueSegregadoService = processarEstoqueSegregadoService;
        this.processarValePermanenteService = processarValePermanenteService;
        this.processamentoService = processamentoService;
        this.processamentoLockRepository = processamentoLockRepository;
    }

    @Scheduled(fixedDelayString = "${etl.scheduler.fixed-delay-ms}")
    public void iniciarCarga() {

        if (!processamentoLockRepository.tentarAdquirirLock(PROCESSAMENTO_LOCK_KEY)) {
            log.info("Processamento ja esta em execucao. Nova execucao ignorada.");
            return;
        }

        Processamento processamento = processamentoService.iniciarProcessamento();

        try {

            processarEstoqueInternoService.processarEstoqueInterno(processamento);
            processarEstoqueSegregadoService.processarEstoqueSegregado(processamento);
            processarValePermanenteService.processarValePermanente(processamento);
            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar o scheduler de ETL.", exception);
            throw exception;

        } finally {

            processamentoLockRepository.liberarLock(PROCESSAMENTO_LOCK_KEY);
        }
    }
}
