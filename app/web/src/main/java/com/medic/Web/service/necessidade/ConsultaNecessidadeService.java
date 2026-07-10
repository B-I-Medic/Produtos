package com.medic.Web.service.necessidade;

import com.medic.Web.dto.necessidade.NecessidadeFilterDTO;
import com.medic.Web.dto.necessidade.NecessidadeAgrupadoResponseDTO;
import com.medic.Web.repository.necessidade.NecessidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaNecessidadeService {

    private final NecessidadeRepository repository;

    public ConsultaNecessidadeService(NecessidadeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Flux<NecessidadeAgrupadoResponseDTO> listNecessidadesAgrupadas(NecessidadeFilterDTO filter) {

        return repository.findByFilter(filter);
    }
}
