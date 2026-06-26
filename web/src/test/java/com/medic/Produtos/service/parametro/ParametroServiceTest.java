package com.medic.Produtos.service.parametro;

import com.medic.Produtos.dto.parametro.periodo.PeriodoRequestDTO;
import com.medic.Produtos.dto.parametro.periodo.PeriodoResponseDTO;
import com.medic.Produtos.dto.parametro.taxa.TaxaRequestDTO;
import com.medic.Produtos.dto.parametro.taxa.TaxaResponseDTO;
import com.medic.Produtos.mapper.parametro.periodo.PeriodoMapper;
import com.medic.Produtos.mapper.parametro.taxa.TaxaMapper;
import com.medic.Produtos.model.parametro.periodo.PeriodoModel;
import com.medic.Produtos.model.parametro.taxa.TaxaModel;
import com.medic.Produtos.repository.parametro.PeriodoRepository;
import com.medic.Produtos.repository.parametro.TaxaRepository;
import com.medic.Produtos.service.parametro.periodo.ManutencaoPeriodoService;
import com.medic.Produtos.service.parametro.taxa.ManutencaoTaxaService;
import com.medic.Produtos.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroServiceTest {

    @Mock
    private PeriodoRepository periodoRepository;
    @Mock
    private PeriodoMapper periodoMapper;
    @Mock
    private TaxaRepository taxaRepository;
    @Mock
    private TaxaMapper taxaMapper;

    @InjectMocks
    private ManutencaoPeriodoService periodoService;
    @InjectMocks
    private ManutencaoTaxaService taxaService;

    @Test
    void shouldDefinirPeriodoAndList() {

        PeriodoModel model = TestDataFactory.periodoModel();
        PeriodoResponseDTO response = TestDataFactory.periodoResponseDTO();
        PeriodoRequestDTO dto = new PeriodoRequestDTO(LocalDate.now(), LocalDate.now().plusDays(1));
        when(periodoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(periodoMapper.map(model, dto, model.getId())).thenReturn(model);
        when(periodoRepository.save(model)).thenReturn(Mono.just(model));
        when(periodoMapper.toDTO(model)).thenReturn(response);
        when(periodoRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));

        StepVerifier.create(periodoService.definirPeriodo(model.getId(), dto, model.getId()))
                .expectNext(response)
                .verifyComplete();

        StepVerifier.create(periodoService.listPeriods())
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldSetTaxaAndList() {

        TaxaModel model = TestDataFactory.taxaModel();
        TaxaResponseDTO response = TestDataFactory.taxaResponseDTO();
        TaxaRequestDTO dto = new TaxaRequestDTO(BigDecimal.ONE);
        when(taxaRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(taxaMapper.update(model, dto, model.getId())).thenReturn(model);
        when(taxaRepository.save(model)).thenReturn(Mono.just(model));
        when(taxaMapper.toDTO(model)).thenReturn(response);
        when(taxaRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));

        StepVerifier.create(taxaService.setRate(model.getId(), dto, model.getId()))
                .expectNext(response)
                .verifyComplete();

        StepVerifier.create(taxaService.listRates())
                .expectNext(response)
                .verifyComplete();
    }
}
