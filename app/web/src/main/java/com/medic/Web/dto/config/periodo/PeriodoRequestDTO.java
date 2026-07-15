package com.medic.Web.dto.config.periodo;

import java.time.LocalDate;

public record PeriodoRequestDTO(

        LocalDate dataInicial,
        LocalDate dataFinal

) {
}
