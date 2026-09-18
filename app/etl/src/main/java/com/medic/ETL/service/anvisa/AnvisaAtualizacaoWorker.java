package com.medic.ETL.service.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.service.schedule.job.AtualizarAnvisaJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class AnvisaAtualizacaoWorker {

    private final AnvisaAtualizacaoClaimService claimService;
    private final AtualizarAnvisaJob job;
    private final ExecutorService executor;
    private final AtomicBoolean tarefaEmExecucao = new AtomicBoolean();

    public AnvisaAtualizacaoWorker(AnvisaAtualizacaoClaimService claimService,
                                   AtualizarAnvisaJob job,
                                   ExecutorService anvisaExecutor) {
        this.claimService = claimService;
        this.job = job;
        this.executor = anvisaExecutor;
    }

    @Scheduled(
            fixedDelayString = "${anvisa.worker.poll-interval-ms:1000}",
            initialDelayString = "${anvisa.worker.initial-delay-ms:1000}"
    )
    public void dispatch() {

        if (!tarefaEmExecucao.compareAndSet(false, true)) {
            return;
        }

        Optional<AnvisaAtualizacaoClaim> claim;

        try {
            claim = claimService.claim();

        } catch (Exception exception) {

            tarefaEmExecucao.set(false);
            log.error("Falha ao obter uma solicitacao pendente da Anvisa.", exception);
            return;
        }

        if (claim.isEmpty()) {
            tarefaEmExecucao.set(false);
            return;
        }

        try {
            executor.submit(() -> {
                try {
                    job.run(claim.get());
                } finally {
                    tarefaEmExecucao.set(false);
                }
            });
        } catch (RuntimeException exception) {
            tarefaEmExecucao.set(false);
            log.error("Falha ao despachar a atualizacao da Anvisa.", exception);
        }
    }
}
