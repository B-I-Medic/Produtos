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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

        PeriodoModel periodWithQuantityOnly = new PeriodoModel();
        periodWithQuantityOnly.setDescricao(PeriodoEnum.ORCAMENTO);
        periodWithQuantityOnly.setQuantidade(7);
        assertThrows(IllegalStateException.class, () -> resolver.resolver(periodWithQuantityOnly));
    }

    @Test
    void shouldRejectNonPositiveQuantity() {

        assertThrows(IllegalArgumentException.class, () -> resolver.calcular(PeriodoTipo.DIAS, 0));
        assertThrows(IllegalArgumentException.class, () -> resolver.calcular(PeriodoTipo.DIAS, null));
        assertThrows(NullPointerException.class, () -> resolver.calcular(null, 1));
        assertThrows(NullPointerException.class, () -> resolver.validar(null, PeriodoTipo.DIAS, 1));
        assertThrows(NullPointerException.class, () -> new PeriodoIntervaloResolver(null));
        assertDoesNotThrow(() -> new PeriodoIntervaloResolver().calcular(PeriodoTipo.DIAS, 1));
    }

    @Test
    void shouldAcceptMatchingHistoricalAndFuturePeriodRules() {
        PeriodoModel historical = new PeriodoModel();
        historical.setDescricao(PeriodoEnum.ORCAMENTO);
        historical.setTipoPeriodo(PeriodoTipo.DIAS);
        historical.setQuantidade(3);
        assertEquals(LocalDate.of(2026, 9, 18), resolver.resolver(historical).dataInicial());

        PeriodoModel future = new PeriodoModel();
        future.setDescricao(PeriodoEnum.AGENDAMENTO);
        future.setTipoPeriodo(PeriodoTipo.PROXIMOS_DIAS);
        future.setQuantidade(3);
        assertEquals(LocalDate.of(2026, 9, 24), resolver.resolver(future).dataFinal());
    }

    @Test
    void shouldAcceptUpcomingClosedMonthsForAgendamento() {

        PeriodoModel future = new PeriodoModel();
        future.setDescricao(PeriodoEnum.AGENDAMENTO);
        future.setTipoPeriodo(PeriodoTipo.PROXIMOS_MESES_FECHADOS);
        future.setQuantidade(2);

        assertEquals(LocalDate.of(2026, 11, 30), resolver.resolver(future).dataFinal());
    }
}
