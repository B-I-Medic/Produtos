package com.medic.ETL.service.periodo;

import java.time.LocalDate;

public record PeriodoIntervalo(

        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
