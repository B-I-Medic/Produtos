package com.medic.Web.service.processamento;

import com.medic.Web.dto.processamento.ProcessamentoResponseDTO;
import com.medic.Web.model.processamento.ProcessamentoEntidade;
import com.medic.Web.repository.processamento.ProcessamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ConsultaProcessamentoService {

    private final ProcessamentoRepository repository;

    public ConsultaProcessamentoService(ProcessamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<ProcessamentoResponseDTO> consultarUltimosPorEntidade() {

        return repository.findUltimosConcluidos()
                .collectMap(ProcessamentoResponseDTO::entidade)
                .flatMapMany(this::completarEntidades);
    }

    private Flux<ProcessamentoResponseDTO> completarEntidades(Map<ProcessamentoEntidade, ProcessamentoResponseDTO> processamentos) {

        return Flux.fromArray(ProcessamentoEntidade.values())
                .map(entidade -> processamentos.getOrDefault(
                        entidade,
                        new ProcessamentoResponseDTO(entidade, null, null, null, null, null)
                ));
    }
}
