package com.medic.Produtos.service.parametro.taxa;

import com.medic.Produtos.dto.parametro.taxa.TaxaRequestDTO;
import com.medic.Produtos.dto.parametro.taxa.TaxaResponseDTO;
import com.medic.Produtos.mapper.parametro.taxa.TaxaMapper;
import com.medic.Produtos.repository.parametro.TaxaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ManutencaoTaxaService {

    private final TaxaRepository repository;
    private final TaxaMapper mapper;

    public ManutencaoTaxaService(TaxaRepository repository, TaxaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Mono<TaxaResponseDTO> setRate(UUID taxaId,
                                         TaxaRequestDTO dto,
                                         UUID userId) {

        return repository.findById(taxaId)
                .map(taxa -> mapper.update(taxa, dto, userId))
                .flatMap(repository::save)
                .map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Flux<TaxaResponseDTO> listRates() {

        return repository.findAll()
                .map(mapper::toDTO);
    }
}
