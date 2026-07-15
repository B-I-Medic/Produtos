package com.medic.Web.mapper.config;

import com.medic.Web.dto.config.periodo.PeriodoRequestDTO;
import com.medic.Web.dto.config.taxa.TaxaRequestDTO;
import com.medic.Web.mapper.config.periodo.PeriodoMapper;
import com.medic.Web.mapper.config.taxa.TaxaMapper;
import com.medic.Web.model.config.periodo.PeriodoEnum;
import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.model.config.taxa.TaxaEnum;
import com.medic.Web.model.config.taxa.TaxaModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParametroMapperTest {

    private final PeriodoMapper periodoMapper = new PeriodoMapper();
    private final TaxaMapper taxaMapper = new TaxaMapper();

    @Test
    void shouldMapPeriodoAndResponse() {

        UUID userId = UUID.randomUUID();
        LocalDate dataInicial = LocalDate.of(2026, 6, 22);
        LocalDate dataFinal = LocalDate.of(2026, 6, 30);
        PeriodoRequestDTO dto = new PeriodoRequestDTO(dataInicial, dataFinal);

        PeriodoModel entity = new PeriodoModel();
        entity.setId(UUID.randomUUID());
        entity.setDescricao(PeriodoEnum.ORCAMENTO);

        PeriodoModel mapped = periodoMapper.map(entity, dto, userId);
        var response = periodoMapper.toDTO(mapped);

        assertEquals(dataInicial, mapped.getDataInicial());
        assertEquals(dataFinal, mapped.getDataFinal());
        assertEquals("20260622", mapped.getDataInicialViman());
        assertEquals("20260630", mapped.getDataFinalViman());
        assertEquals(userId, mapped.getAtualizadoPor());
        assertEquals(entity.getId(), response.id());
        assertEquals(PeriodoEnum.ORCAMENTO, response.descricao());
        assertEquals(dataInicial, response.dataInicial());
        assertEquals(dataFinal, response.dataFinal());
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
