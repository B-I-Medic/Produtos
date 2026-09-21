package com.medic.ETL.service.periodo;

import com.medic.ETL.model.periodo.PeriodoTipo;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Component
public class PeriodoIntervaloResolver {

    public static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final Clock clock;

    public PeriodoIntervaloResolver() {
        this(Clock.system(ZONE_ID));
    }

    public PeriodoIntervaloResolver(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock é obrigatório");
    }

    public PeriodoIntervalo calcular(PeriodoTipo tipo, Integer quantidade) {

        Objects.requireNonNull(tipo, "tipo do período é obrigatório");

        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException("A quantidade do período deve ser maior que zero");
        }

        LocalDate hoje = LocalDate.now(clock);

        return switch (tipo) {
            case DIAS -> calcularDiasFechados(hoje, quantidade);
            case MESES_FECHADOS -> calcularMesesFechados(hoje, quantidade);
            case PROXIMOS_DIAS -> calcularProximosDias(hoje, quantidade);
            case PROXIMOS_MESES_FECHADOS -> calcularProximosMesesFechados(hoje, quantidade);
        };
    }

    public void validar(String descricao, PeriodoTipo tipo, Integer quantidade) {

        Objects.requireNonNull(descricao, "descrição do período é obrigatória");
        calcular(tipo, quantidade);

        boolean agendamento = "AGENDAMENTO".equals(descricao);
        boolean tipoFuturo = tipo == PeriodoTipo.PROXIMOS_DIAS
                || tipo == PeriodoTipo.PROXIMOS_MESES_FECHADOS;

        if (agendamento != tipoFuturo) {
            throw new IllegalArgumentException("AGENDAMENTO deve usar uma regra futura e os demais períodos uma regra histórica");
        }
    }

    private PeriodoIntervalo calcularDiasFechados(LocalDate hoje, int quantidade) {

        LocalDate dataFinal = hoje.minusDays(1);
        LocalDate dataInicial = dataFinal.minusDays(quantidade - 1L);

        return new PeriodoIntervalo(dataInicial, dataFinal);
    }

    private PeriodoIntervalo calcularMesesFechados(LocalDate hoje, int quantidade) {

        LocalDate dataFinal = hoje.withDayOfMonth(1).minusDays(1);
        LocalDate dataInicial = dataFinal.withDayOfMonth(1).minusMonths(quantidade - 1L);

        return new PeriodoIntervalo(dataInicial, dataFinal);
    }

    private PeriodoIntervalo calcularProximosDias(LocalDate hoje, int quantidade) {

        LocalDate dataInicial = hoje.plusDays(1);
        LocalDate dataFinal = hoje.plusDays(quantidade);

        return new PeriodoIntervalo(dataInicial, dataFinal);
    }

    private PeriodoIntervalo calcularProximosMesesFechados(LocalDate hoje, int quantidade) {

        LocalDate dataInicial = hoje.withDayOfMonth(1).plusMonths(1);
        LocalDate dataFinal = dataInicial.plusMonths(quantidade).minusDays(1);

        return new PeriodoIntervalo(dataInicial, dataFinal);
    }
}
