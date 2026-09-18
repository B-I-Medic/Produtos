package com.medic.ETL.service.anvisa;

import com.medic.ETL.config.property.AnvisaProperties;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoClaim;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.model.anvisa.AnvisaAtualizacaoStatus;
import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoDisparo;
import com.medic.ETL.model.processamento.ProcessamentoEntidade;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.service.processamento.ControlarProcessamentoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnvisaAtualizacaoClaimService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private static final String SAME_DAY_INTERRUPTED_ERROR =
            "A execucao anterior foi interrompida; uma nova tentativa foi iniciada no mesmo dia.";
    private static final String STALE_REQUESTED_ERROR =
            "A solicitacao nao foi iniciada antes do encerramento do dia de referencia.";
    private static final String STALE_RUNNING_ERROR =
            "A execucao foi interrompida e nao sera retomada em outro dia.";

    @PersistenceContext
    private EntityManager entityManager;

    private final AnvisaAtualizacaoRepository atualizacaoRepository;
    private final ControlarProcessamentoService processamentoService;
    private final long leaseSeconds;

    public AnvisaAtualizacaoClaimService(AnvisaAtualizacaoRepository atualizacaoRepository,
                                         ControlarProcessamentoService processamentoService,
                                         AnvisaProperties properties) {
        this.atualizacaoRepository = atualizacaoRepository;
        this.processamentoService = processamentoService;
        this.leaseSeconds = properties.getWorker().getLeaseSeconds();
    }

    @Transactional
    public Optional<AnvisaAtualizacaoClaim> claim() {

        Instant now = Instant.now();
        LocalDate currentDate = now.atZone(ZONE_ID).toLocalDate();

        Optional<AnvisaAtualizacaoRegistro> candidate = atualizacaoRepository.findNextForClaim(now, currentDate);

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        AnvisaAtualizacaoRegistro registro = candidate.get();

        if (registro.dataReferencia().isBefore(currentDate)) {
            failStale(registro, now);
            return Optional.empty();
        }

        if (registro.status() == AnvisaAtualizacaoStatus.EM_EXECUCAO) {
            atualizacaoRepository.markInterruptedAttemptAsFailed(registro.processamentoId(), now);
            atualizacaoRepository.resetExpiredForRetry(registro.id(), now, SAME_DAY_INTERRUPTED_ERROR);
        }

        Processamento processamento = processamentoService.iniciarProcessamento(
                ProcessamentoEntidade.ANVISA,
                ProcessamentoDisparo.MANUAL
        );

        entityManager.flush();

        UUID leaseToken = UUID.randomUUID();

        atualizacaoRepository.start(
                registro.id(),
                processamento.getId(),
                leaseToken,
                now,
                now.plusSeconds(Math.max(60, leaseSeconds))
        );

        return Optional.of(new AnvisaAtualizacaoClaim(registro.id(), leaseToken, processamento));
    }

    private void failStale(AnvisaAtualizacaoRegistro registro, Instant now) {

        String error = STALE_REQUESTED_ERROR;

        if (registro.status() == AnvisaAtualizacaoStatus.EM_EXECUCAO) {
            atualizacaoRepository.markInterruptedAttemptAsFailed(registro.processamentoId(), now);
            error = STALE_RUNNING_ERROR;
        }

        atualizacaoRepository.failStale(registro.id(), now, error);
    }
}
