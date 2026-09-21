package com.medic.ETL.service.anvisa;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoRegistro;
import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoResponse;
import com.medic.ETL.dto.anvisa.AnvisaSolicitacaoResultado;
import com.medic.ETL.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.ETL.repository.anvisa.AnvisaConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class SolicitarAnvisaAtualizacaoService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AnvisaAtualizacaoRepository atualizacaoRepository;
    private final AnvisaConfiguracaoRepository configuracaoRepository;

    public SolicitarAnvisaAtualizacaoService(AnvisaAtualizacaoRepository atualizacaoRepository,
                                             AnvisaConfiguracaoRepository configuracaoRepository) {
        this.atualizacaoRepository = atualizacaoRepository;
        this.configuracaoRepository = configuracaoRepository;
    }

    @Transactional
    public AnvisaSolicitacaoResultado solicitar(UUID solicitadoPor) {

        Instant now = Instant.now();
        LocalDate date = now.atZone(ZONE_ID).toLocalDate();

        atualizacaoRepository.insertIfAbsent(UUID.randomUUID(), date, solicitadoPor, now);

        AnvisaAtualizacaoRegistro registro = atualizacaoRepository.findByDateForUpdate(date)
                .orElseThrow(() -> new IllegalStateException("Nao foi possivel obter o controle diario da Anvisa."));

        return switch (registro.status()) {
            case CONCLUIDA -> new AnvisaSolicitacaoResultado(
                    response(registro, null),
                    AnvisaSolicitacaoResultado.Tipo.CONCLUIDA
            );
            case FALHOU -> solicitarAposFalha(registro, solicitadoPor, now);
            case SOLICITADA, EM_EXECUCAO -> new AnvisaSolicitacaoResultado(
                    response(registro, null),
                    AnvisaSolicitacaoResultado.Tipo.ACEITA
            );
        };
    }

    private AnvisaSolicitacaoResultado solicitarAposFalha(AnvisaAtualizacaoRegistro registro,
                                                          UUID solicitadoPor,
                                                          Instant now) {

        int cooldownMinutos = configuracaoRepository.getRetryCooldownMinutos();
        Instant proximaSolicitacao = registro.ultimaFalhaEm()
                .plusSeconds(cooldownMinutos * 60L);

        if (now.isBefore(proximaSolicitacao)) {
            return new AnvisaSolicitacaoResultado(
                    response(registro, proximaSolicitacao),
                    AnvisaSolicitacaoResultado.Tipo.COOLDOWN
            );
        }

        atualizacaoRepository.reabrir(registro.id(), solicitadoPor, now);

        AnvisaAtualizacaoRegistro reaberta = atualizacaoRepository.findByDateForUpdate(registro.dataReferencia())
                .orElseThrow(() -> new IllegalStateException("A atualizacao da Anvisa nao foi reaberta."));

        return new AnvisaSolicitacaoResultado(
                response(reaberta, null),
                AnvisaSolicitacaoResultado.Tipo.ACEITA
        );
    }

    private AnvisaAtualizacaoResponse response(AnvisaAtualizacaoRegistro registro,
                                               Instant proximaSolicitacao) {

        return new AnvisaAtualizacaoResponse(
                registro.id(),
                registro.dataReferencia(),
                registro.status().name(),
                registro.tentativas(),
                registro.solicitadoEm(),
                registro.iniciadoEm(),
                registro.concluidoEm(),
                registro.ultimaFalhaEm(),
                proximaSolicitacao,
                registro.processamentoId(),
                registro.ultimoErro()
        );
    }
}
