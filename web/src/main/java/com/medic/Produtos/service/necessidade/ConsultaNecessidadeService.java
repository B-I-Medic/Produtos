package com.medic.Produtos.service.necessidade;

import com.medic.Produtos.dto.necessidade.NecessidadeResponseDTO;
import com.medic.Produtos.mapper.necessidade.NecessidadeMapper;
import com.medic.Produtos.repository.necessidade.NecessidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
public class ConsultaNecessidadeService {

    private final NecessidadeRepository repository;
    private final NecessidadeMapper mapper;

    public ConsultaNecessidadeService(NecessidadeRepository repository,
                                      NecessidadeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Flux<NecessidadeResponseDTO> listNecessidades() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
