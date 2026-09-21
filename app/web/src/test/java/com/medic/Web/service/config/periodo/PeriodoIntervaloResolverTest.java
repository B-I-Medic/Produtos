package com.medic.Web.service.config.periodo;

import com.medic.Web.model.config.periodo.PeriodoModel;
import com.medic.Web.model.config.periodo.PeriodoEnum;
import com.medic.Web.model.config.periodo.PeriodoTipo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodoIntervaloResolverTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-21T12:00:00Z"),
            ZoneId.of("America/Sao_Paulo")
    );

    private final PeriodoIntervaloResolver resolver = new PeriodoIntervaloResolver(CLOCK);

    @Test
    void shouldCalculateClosedDays() {

        PeriodoIntervalo intervalo = resolver.calcular(PeriodoTipo.DIAS, 7);

        assertEquals(LocalDate.of(2026, 9, 14), intervalo.dataInicial());
        assertEquals(LocalDate.of(2026, 9, 20), intervalo.dataFinal());
    }

    @Test
    void shouldCalculateClosedMonths() {

        PeriodoIntervalo intervalo = resolver.calcular(PeriodoTipo.MESES_FECHADOS, 3);

        assertEquals(LocalDate.of(2026, 6, 1), intervalo.dataInicial());
        assertEquals(LocalDate.of(2026, 8, 31), intervalo.dataFinal());
    }

    @Test
    void shouldCalculateNextDaysFromTomorrow() {

        PeriodoIntervalo intervalo = resolver.calcular(PeriodoTipo.PROXIMOS_DIAS, 7);

        assertEquals(LocalDate.of(2026, 9, 22), intervalo.dataInicial());
        assertEquals(LocalDate.of(2026, 9, 28), intervalo.dataFinal());
    }

    @Test
    void shouldCalculateNextClosedMonths() {

        PeriodoIntervalo intervalo = resolver.calcular(PeriodoTipo.PROXIMOS_MESES_FECHADOS, 3);

        assertEquals(LocalDate.of(2026, 10, 1), intervalo.dataInicial());
        assertEquals(LocalDate.of(2026, 12, 31), intervalo.dataFinal());
    }

    @Test
    void shouldRejectHistoricalRuleForAgendamento() {

        PeriodoModel periodo = new PeriodoModel();
        periodo.setDescricao(PeriodoEnum.AGENDAMENTO);
        periodo.setTipoPeriodo(PeriodoTipo.DIAS);
        periodo.setQuantidade(7);

        assertThrows(IllegalArgumentException.class, () -> resolver.resolver(periodo));
    }

    @Test
    void shouldRejectFutureRuleForHistoricalPeriod() {

        PeriodoModel periodo = new PeriodoModel();
        periodo.setDescricao(PeriodoEnum.ORCAMENTO);
        periodo.setTipoPeriodo(PeriodoTipo.PROXIMOS_DIAS);
        periodo.setQuantidade(7);

        assertThrows(IllegalArgumentException.class, () -> resolver.resolver(periodo));
    }

    @Test
    void shouldResolveLegacyFixedPeriod() {

        PeriodoModel periodo = new PeriodoModel();
        periodo.setDataInicial(LocalDate.of(2026, 1, 1));
        periodo.setDataFinal(LocalDate.of(2026, 1, 31));

        PeriodoIntervalo intervalo = resolver.resolver(periodo);

        assertEquals(LocalDate.of(2026, 1, 1), intervalo.dataInicial());
        assertEquals(LocalDate.of(2026, 1, 31), intervalo.dataFinal());
    }

    @Test
    void shouldRejectIncompletePeriodConfiguration() {

        PeriodoModel periodo = new PeriodoModel();
        periodo.setTipoPeriodo(PeriodoTipo.DIAS);

        assertThrows(IllegalStateException.class, () -> resolver.resolver(periodo));
    }

    @Test
    void shouldRejectNonPositiveQuantity() {

        assertThrows(IllegalArgumentException.class, () -> resolver.calcular(PeriodoTipo.DIAS, 0));
    }
}
