package com.medic.ETL.schedule;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.processamento.ProcessamentoRepository;
import com.medic.ETL.service.demanda.ProcessarDemandaService;
import com.medic.ETL.service.estoque.interno.ProcessarEstoqueInternoService;
import com.medic.ETL.service.estoque.segregado.ProcessarEstoqueSegregadoService;
import com.medic.ETL.service.estoque.valePermanente.ProcessarValePermanenteService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.service.produto.ProcessarProdutoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Schedule {

    private static final long ESTOQUE_LOCK_KEY = 872341L;
    private static final long PRODUTO_LOCK_KEY = 872342L;
    private static final long DEMANDA_LOCK_KEY = 872343L;

    private final ProcessamentoRepository processamentoRepository;

    private final ControlarProcessamentoService processamentoService;
    private final ProcessarEstoqueInternoService processarEstoqueInternoService;
    private final ProcessarEstoqueSegregadoService processarEstoqueSegregadoService;
    private final ProcessarValePermanenteService processarValePermanenteService;
    private final ProcessarProdutoService processarProdutoService;
    private final ProcessarDemandaService processarDemandaService;

    public Schedule(ProcessarEstoqueInternoService processarEstoqueInternoService,
                    ProcessarEstoqueSegregadoService processarEstoqueSegregadoService,
                    ProcessarValePermanenteService processarValePermanenteService,
                    ProcessarProdutoService processarProdutoService,
                    ControlarProcessamentoService processamentoService,
                    ProcessamentoRepository processamentoRepository,
                    ProcessarDemandaService processarDemandaService) {
        this.processarEstoqueInternoService = processarEstoqueInternoService;
        this.processarEstoqueSegregadoService = processarEstoqueSegregadoService;
        this.processarValePermanenteService = processarValePermanenteService;
        this.processarProdutoService = processarProdutoService;
        this.processamentoService = processamentoService;
        this.processamentoRepository = processamentoRepository;
        this.processarDemandaService = processarDemandaService;
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void atualizarEstoque() {

        log.info("Iniciando Estoque");

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

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar atualização de estoque", exception);
            throw exception;

        } finally {

            processamentoRepository.liberarLock(ESTOQUE_LOCK_KEY);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void atualizarProdutos() {

        if (processamentoRepository.lockEmUso(PRODUTO_LOCK_KEY)) {

            processamentoService.abortarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO);
            log.info("Processamento de produtos ja esta em execucao. Nova execucao abortada");

            return;
        }

        Processamento processamento = processamentoService.iniciarProcessamento(ProcessamentoEntidade.PRODUTOS, ProcessamentoDisparo.AUTOMATICO);

        try {

            processarProdutoService.atualizarProdutos(processamento);
            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.CONCLUIDO);

        } catch (Exception exception) {

            processamentoService.encerrarProcessamento(processamento, ProcessamentoStatus.FALHOU);
            log.error("Falha ao executar a atualizacao de produtos.", exception);
            throw exception;

        } finally {

            processamentoRepository.liberarLock(PRODUTO_LOCK_KEY);
        }
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void atualizarDemanda() {


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
