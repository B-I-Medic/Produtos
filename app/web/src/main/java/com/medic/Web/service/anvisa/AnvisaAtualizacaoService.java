package com.medic.Web.service.anvisa;

import com.medic.Web.cliente.AnvisaEtlClient;
import com.medic.Web.dto.anvisa.AnvisaAtualizacaoResponseDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoRequestDTO;
import com.medic.Web.dto.anvisa.AnvisaConfiguracaoResponseDTO;
import com.medic.Web.exception.type.AnvisaEtlUnavailableException;
import com.medic.Web.exception.type.NotFoundException;
import com.medic.Web.repository.anvisa.AnvisaAtualizacaoRepository;
import com.medic.Web.repository.anvisa.AnvisaConfiguracaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AnvisaAtualizacaoService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AnvisaEtlClient etlClient;
    private final AnvisaAtualizacaoRepository atualizacaoRepository;
    private final AnvisaConfiguracaoRepository configuracaoRepository;

    public AnvisaAtualizacaoService(AnvisaEtlClient etlClient,
                                    AnvisaAtualizacaoRepository atualizacaoRepository,
                                    AnvisaConfiguracaoRepository configuracaoRepository) {
        this.etlClient = etlClient;
        this.atualizacaoRepository = atualizacaoRepository;
        this.configuracaoRepository = configuracaoRepository;
    }

    public Mono<ResponseEntity<AnvisaAtualizacaoResponseDTO>> solicitar(UUID usuarioId) {

        return etlClient.solicitar(usuarioId)
                .map(result -> {

                    if (result.status().is2xxSuccessful()
                            || result.status().value() == HttpStatus.CONFLICT.value()
                            || result.status().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {

                        ResponseEntity.BodyBuilder response = ResponseEntity.status(result.status());

                        if (result.retryAfter() != null) {
                            response.header("Retry-After", result.retryAfter());
                        }

                        return response.body(result.body());
                    }

                    throw new AnvisaEtlUnavailableException();

                })
                .onErrorMap(exception -> exception instanceof AnvisaEtlUnavailableException
                        ? exception
                        : new AnvisaEtlUnavailableException());
    }

    public Flux<ServerSentEvent<AnvisaAtualizacaoResponseDTO>> acompanhar(UUID id) {

        return atualizacaoRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("atualizacao da Anvisa", id.toString(), "id")))
                .flatMapMany(primeiro -> Flux.concat(
                                Mono.just(primeiro),
                                Flux.interval(Duration.ofSeconds(1))
                                        .flatMap(ignored -> atualizacaoRepository.findById(id))
                        )
                        .distinctUntilChanged()
                        .takeUntil(this::terminal)
                        .map(status -> ServerSentEvent.<AnvisaAtualizacaoResponseDTO>builder()
                                .event("anvisa-atualizacao")
                                .id(status.id().toString())
                                .data(status)
                                .build()));
    }

    public Mono<AnvisaAtualizacaoResponseDTO> hoje() {

        return atualizacaoRepository.findToday(LocalDate.now(ZONE_ID));
    }

    public Mono<AnvisaConfiguracaoResponseDTO> configuracao() {

        return configuracaoRepository.find();
    }

    public Mono<AnvisaConfiguracaoResponseDTO> atualizarConfiguracao(AnvisaConfiguracaoRequestDTO request,
                                                                     UUID usuarioId) {

        return configuracaoRepository.update(request.retryCooldownMinutos(), usuarioId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Configuracao da Anvisa nao encontrada.")));
    }

    private boolean terminal(AnvisaAtualizacaoResponseDTO status) {

        return "CONCLUIDA".equals(status.status()) || "FALHOU".equals(status.status());
    }
}
