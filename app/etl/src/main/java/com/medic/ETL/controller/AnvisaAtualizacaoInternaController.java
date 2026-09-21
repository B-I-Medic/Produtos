package com.medic.ETL.controller;

import com.medic.ETL.dto.anvisa.AnvisaAtualizacaoResponse;
import com.medic.ETL.dto.anvisa.AnvisaSolicitacaoResultado;
import com.medic.ETL.dto.anvisa.SolicitarAnvisaAtualizacaoRequest;
import com.medic.ETL.service.anvisa.SolicitarAnvisaAtualizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/internal/anvisa")
public class AnvisaAtualizacaoInternaController {

    private final SolicitarAnvisaAtualizacaoService service;

    public AnvisaAtualizacaoInternaController(SolicitarAnvisaAtualizacaoService service) {
        this.service = service;
    }

    @PostMapping("/atualizacoes")
    public ResponseEntity<AnvisaAtualizacaoResponse> solicitar(@Valid @RequestBody SolicitarAnvisaAtualizacaoRequest request) {

        AnvisaSolicitacaoResultado result = service.solicitar(request.solicitadoPor());

        if (result.tipo() == AnvisaSolicitacaoResultado.Tipo.COOLDOWN) {
            long retryAfter = Math.max(
                    1,
                    Duration.between(Instant.now(), result.response().proximaSolicitacaoEm()).getSeconds()
            );

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
                    .body(result.response());
        }

        if (result.tipo() == AnvisaSolicitacaoResultado.Tipo.CONCLUIDA) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.response());
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result.response());
    }
}
