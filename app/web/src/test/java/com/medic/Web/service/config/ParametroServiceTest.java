package com.medic.Web.service.config;

import com.medic.Web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.config.periodo.PeriodoResponseDTO;
import com.medic.Web.dto.config.taxa.TaxaRequestDTO;
import com.medic.Web.dto.config.taxa.TaxaResponseDTO;
import com.medic.Web.mapper.config.periodo.PeriodoMapper;
import com.medic.Web.mapper.config.taxa.TaxaMapper;
import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.model.config.taxa.TaxaModel;
import com.medic.Web.repository.config.PeriodoRepository;
import com.medic.Web.repository.config.TaxaRepository;
import com.medic.Web.service.config.periodo.ManutencaoPeriodoService;
import com.medic.Web.service.config.taxa.ManutencaoTaxaService;
import com.medic.Web.support.TestDataFactory;
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
    void shouldDefinirPeriodo() {

        PeriodoModel model = TestDataFactory.periodoModel();
        PeriodoResponseDTO response = TestDataFactory.periodoResponseDTO();
        PeriodoRequestDTO dto = new PeriodoRequestDTO(LocalDate.now(), LocalDate.now().plusDays(1));
        when(periodoRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(periodoMapper.map(model, dto, model.getId())).thenReturn(model);
        when(periodoRepository.save(model)).thenReturn(Mono.just(model));
        when(periodoMapper.toDTO(model)).thenReturn(response);
        StepVerifier.create(periodoService.definirPeriodo(model.getId(), dto, model.getId()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldListPeriodos() {

        PeriodoModel model = TestDataFactory.periodoModel();
        PeriodoResponseDTO response = TestDataFactory.periodoResponseDTO();
        when(periodoRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(periodoMapper.toDTO(model)).thenReturn(response);

        StepVerifier.create(periodoService.listPeriods())
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldSetTaxa() {

        TaxaModel model = TestDataFactory.taxaModel();
        TaxaResponseDTO response = TestDataFactory.taxaResponseDTO();
        TaxaRequestDTO dto = new TaxaRequestDTO(BigDecimal.ONE);
        when(taxaRepository.findById(model.getId())).thenReturn(Mono.just(model));
        when(taxaMapper.update(model, dto, model.getId())).thenReturn(model);
        when(taxaRepository.save(model)).thenReturn(Mono.just(model));
        when(taxaMapper.toDTO(model)).thenReturn(response);
        StepVerifier.create(taxaService.setRate(model.getId(), dto, model.getId()))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldListTaxas() {

        TaxaModel model = TestDataFactory.taxaModel();
        TaxaResponseDTO response = TestDataFactory.taxaResponseDTO();
        when(taxaRepository.findAll()).thenReturn(Flux.fromIterable(List.of(model)));
        when(taxaMapper.toDTO(model)).thenReturn(response);

        StepVerifier.create(taxaService.listRates())
                .expectNext(response)
                .verifyComplete();
    }
}
