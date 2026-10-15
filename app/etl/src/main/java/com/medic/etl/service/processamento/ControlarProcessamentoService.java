package com.medic.etl.service.processamento;

import com.medic.etl.model.processamento.Processamento;
import com.medic.etl.model.processamento.ProcessamentoDisparo;
import com.medic.etl.model.processamento.ProcessamentoEntidade;
import com.medic.etl.model.processamento.ProcessamentoStatus;
import com.medic.etl.repository.processamento.ProcessamentoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ControlarProcessamentoService {

    private final ProcessamentoRepository repository;

    public ControlarProcessamentoService(ProcessamentoRepository repository) {
        this.repository = repository;
    }

    public Processamento iniciarProcessamento(ProcessamentoEntidade entidade, ProcessamentoDisparo disparo) {

        Processamento processamento = new Processamento();

        processamento.setIniciadoEm(Instant.now());
        processamento.setStatus(ProcessamentoStatus.INICIADO);
        processamento.setEntidade(entidade);
        processamento.setTipoDisparo(disparo);

        return repository.save(processamento);
    }

    public void abortarProcessamento(ProcessamentoEntidade entidade, ProcessamentoDisparo disparo) {

        Processamento processamento = new Processamento();

        processamento.setIniciadoEm(Instant.now());
        processamento.setConcluidoEm(Instant.now());
        processamento.setStatus(ProcessamentoStatus.ABORTADO);
        processamento.setEntidade(entidade);
        processamento.setTipoDisparo(disparo);

        repository.save(processamento);
    }

    public void encerrarProcessamento(Processamento processamento, ProcessamentoStatus status) {

        if (processamento == null) return;

        processamento.setStatus(status);
        processamento.setConcluidoEm(Instant.now());

        repository.save(processamento);
    }
}
