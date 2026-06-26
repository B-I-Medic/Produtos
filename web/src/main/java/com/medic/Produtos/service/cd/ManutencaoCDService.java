package com.medic.Produtos.service.cd;

import com.medic.Produtos.dto.cd.CentroDistribuicaoRequestDTO;
import com.medic.Produtos.dto.cd.CentroDistribuicaoResponseDTO;
import com.medic.Produtos.mapper.cd.CentroDistribuicaoMapper;
import com.medic.Produtos.model.cd.CentroDistribuicaoModel;
import com.medic.Produtos.repository.cd.CentroDistribuicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoCDService {

    private final CentroDistribuicaoRepository repository;
    private final CentroDistribuicaoMapper mapper;

    public ManutencaoCDService(CentroDistribuicaoRepository repository,
                               CentroDistribuicaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Mono<CentroDistribuicaoResponseDTO> save(CentroDistribuicaoRequestDTO dto,
                                                    UUID userId) {

        return Mono.just(new CentroDistribuicaoModel())
                .map(cd -> mapper.toEntity(cd, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<CentroDistribuicaoResponseDTO> update(UUID cdId,
                                                      CentroDistribuicaoRequestDTO dto,
                                                      UUID userId) {

        return repository.findById(cdId)
                .map(cd -> mapper.toEntity(cd, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional
    public Mono<Void> delete(UUID cdId) {

        return repository.deleteById(cdId);
    }

    @Transactional(readOnly = true)
    public Flux<CentroDistribuicaoResponseDTO> listCDs() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
