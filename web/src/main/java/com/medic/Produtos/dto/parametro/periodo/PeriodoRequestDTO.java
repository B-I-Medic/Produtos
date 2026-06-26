package com.medic.Produtos.dto.parametro.periodo;

import java.time.LocalDate;

public record PeriodoRequestDTO(

        LocalDate dataInicial,
        LocalDate dataFinal

) {
}
