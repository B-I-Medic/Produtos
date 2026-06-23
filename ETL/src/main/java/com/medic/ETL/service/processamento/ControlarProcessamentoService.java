package com.medic.ETL.service.processamento;

import com.medic.ETL.model.processamento.Processamento;
import com.medic.ETL.model.processamento.ProcessamentoStatus;
import com.medic.ETL.repository.processamento.ProcessamentoRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ControlarProcessamentoService {

    private final ProcessamentoRepository processamentoRepository;

    public ControlarProcessamentoService(ProcessamentoRepository processamentoRepository) {
        this.processamentoRepository = processamentoRepository;
    }

    public Processamento iniciarProcessamento() {

        Processamento processamento = new Processamento();
        processamento.setIniciadoEm(OffsetDateTime.now());
        processamento.setStatus(ProcessamentoStatus.INICIADO);

        return processamentoRepository.save(processamento);
    }

    public void encerrarProcessamento(Processamento processamento, ProcessamentoStatus status) {

        processamento.setStatus(status);
        processamento.setConcluidoEm(OffsetDateTime.now());
        processamentoRepository.save(processamento);
    }
}
