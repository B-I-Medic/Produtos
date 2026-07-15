package com.medic.Web.dto.config.periodo;

import com.medic.Web.model.config.periodo.PeriodoEnum;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoResponseDTO(

        UUID id,
        PeriodoEnum descricao,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
