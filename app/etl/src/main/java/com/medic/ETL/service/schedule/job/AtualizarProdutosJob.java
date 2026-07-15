package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.model.schedule.ScheduleJob;
import com.medic.ETL.repository.processamento.ProcessamentoCustomRepository;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import com.medic.ETL.service.produto.ProcessarProdutoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AtualizarProdutosJob implements Job {

    private static final long PRODUTO_LOCK_KEY = 872342L;

    private final ProcessamentoCustomRepository processamentoRepository;
    private final ControlarProcessamentoService processamentoService;
    private final ProcessarProdutoService processarProdutoService;

    public AtualizarProdutosJob(ProcessamentoCustomRepository processamentoRepository,
                                ControlarProcessamentoService processamentoService, ProcessarProdutoService processarProdutoService) {
        this.processamentoRepository = processamentoRepository;
        this.processamentoService = processamentoService;
        this.processarProdutoService = processarProdutoService;
    }

    @Override
    public ScheduleJob getJob() {

        return ScheduleJob.ATUALIZAR_PRODUTOS;
    }

    @Override
    public void run() {

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
}
