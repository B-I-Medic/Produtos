package com.medic.Web.mapper.config;

import com.medic.Web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.config.taxa.TaxaRequestDTO;
import com.medic.Web.mapper.config.periodo.PeriodoMapper;
import com.medic.Web.mapper.config.taxa.TaxaMapper;
import com.medic.Web.model.config.periodo.PeriodoEnum;
import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.model.config.periodo.PeriodoTipo;
import com.medic.Web.service.config.periodo.PeriodoIntervaloResolver;
import com.medic.Web.model.config.taxa.TaxaEnum;
import com.medic.Web.model.config.taxa.TaxaModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParametroMapperTest {

    private final PeriodoMapper periodoMapper = new PeriodoMapper(
            new PeriodoIntervaloResolver(
                    Clock.fixed(
                            Instant.parse("2026-06-21T12:00:00Z"),
                            ZoneId.of("America/Sao_Paulo")
                    )
            )
    );
    private final TaxaMapper taxaMapper = new TaxaMapper();

    @Test
    void shouldMapPeriodoAndResponse() {

        UUID userId = UUID.randomUUID();
        LocalDate dataInicial = LocalDate.of(2026, 6, 14);
        LocalDate dataFinal = LocalDate.of(2026, 6, 20);
        PeriodoRequestDTO dto = new PeriodoRequestDTO(PeriodoTipo.DIAS, 7);

        PeriodoModel entity = new PeriodoModel();
        entity.setId(UUID.randomUUID());
        entity.setDescricao(PeriodoEnum.ORCAMENTO);

        PeriodoModel mapped = periodoMapper.map(entity, dto, userId);
        var response = periodoMapper.toDTO(mapped);

        assertEquals(PeriodoTipo.DIAS, mapped.getTipoPeriodo());
        assertEquals(7, mapped.getQuantidade());
        assertEquals(null, mapped.getDataInicial());
        assertEquals(null, mapped.getDataFinal());
        assertEquals(null, mapped.getDataInicialViman());
        assertEquals(null, mapped.getDataFinalViman());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertEquals(entity.getId(), response.id());
        assertEquals(PeriodoEnum.ORCAMENTO, response.descricao());
        assertEquals(PeriodoTipo.DIAS, response.tipo());
        assertEquals(7, response.quantidade());
        assertEquals(dataInicial, response.dataInicial());
        assertEquals(dataFinal, response.dataFinal());
    }

    @Test
    void shouldMapFutureAgendamentoAndResponse() {

        UUID userId = UUID.randomUUID();
        PeriodoRequestDTO dto = new PeriodoRequestDTO(PeriodoTipo.PROXIMOS_DIAS, 7);

        PeriodoModel entity = new PeriodoModel();
        entity.setId(UUID.randomUUID());
        entity.setDescricao(PeriodoEnum.AGENDAMENTO);

        PeriodoModel mapped = periodoMapper.map(entity, dto, userId);
        var response = periodoMapper.toDTO(mapped);

        assertEquals(PeriodoTipo.PROXIMOS_DIAS, mapped.getTipoPeriodo());
        assertEquals(7, mapped.getQuantidade());
        assertEquals(PeriodoEnum.AGENDAMENTO, response.descricao());
        assertEquals(PeriodoTipo.PROXIMOS_DIAS, response.tipo());
        assertEquals(LocalDate.of(2026, 6, 22), response.dataInicial());
        assertEquals(LocalDate.of(2026, 6, 28), response.dataFinal());
    }

    @Test
    void shouldMapTaxaAndResponse() {

        UUID userId = UUID.randomUUID();
        TaxaRequestDTO dto = new TaxaRequestDTO(BigDecimal.valueOf(1.75));

        TaxaModel entity = new TaxaModel();
        entity.setId(UUID.randomUUID());
        entity.setDescricao(TaxaEnum.ORCAMENTO);

        TaxaModel mapped = taxaMapper.update(entity, dto, userId);
        var response = taxaMapper.toDTO(mapped);

        assertEquals(BigDecimal.valueOf(1.75), mapped.getTaxa());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertEquals(entity.getId(), response.id());
        assertEquals(TaxaEnum.ORCAMENTO, response.descricao());
        assertEquals(BigDecimal.valueOf(1.75), response.taxa());
    }
}
