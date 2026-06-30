package com.medic.Web.dto.parametro.periodo;

import com.medic.Web.model.parametro.periodo.PeriodoEnum;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoResponseDTO(

        UUID id,
        PeriodoEnum descricao,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
