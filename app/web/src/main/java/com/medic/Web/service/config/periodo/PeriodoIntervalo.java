package com.medic.Web.service.config.periodo;

import java.time.LocalDate;

public record PeriodoIntervalo(

        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
