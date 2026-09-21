package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.service.anvisa.AtualizarAnvisaService;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class AtualizarAnvisaJob {

    private final ControlarProcessamentoService processamentoService;
    private final AtualizarAnvisaService atualizarAnvisaService;
    private final AnvisaAtualizacaoRepository atualizacaoRepository;

    public AtualizarAnvisaJob(ControlarProcessamentoService processamentoService,
                              AtualizarAnvisaService atualizarAnvisaService,
                              AnvisaAtualizacaoRepository atualizacaoRepository) {
        this.processamentoService = processamentoService;
        this.atualizarAnvisaService = atualizarAnvisaService;
        this.atualizacaoRepository = atualizacaoRepository;
    }

    public void run(AnvisaAtualizacaoClaim claim) {

        try {

            atualizarAnvisaService.atualizar(claim.processamento());
            processamentoService.encerrarProcessamento(claim.processamento(), ProcessamentoStatus.CONCLUIDO);
            atualizacaoRepository.complete(claim.atualizacaoId(), claim.leaseToken(), Instant.now());
            log.info("Carga da Anvisa concluida com sucesso. Atualizacao {}", claim.atualizacaoId());

        } catch (Exception exception) {
            processamentoService.encerrarProcessamento(claim.processamento(), ProcessamentoStatus.FALHOU);
            atualizacaoRepository.fail(
                    claim.atualizacaoId(),
                    claim.leaseToken(),
                    Instant.now(),
                    mensagemErro(exception)
            );
            log.error("Falha ao executar a atualizacao da Anvisa.", exception);
        }
    }

    private String mensagemErro(Exception exception) {

        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
