package com.medic.Web.dto.config.periodo;

import com.medic.Web.model.config.periodo.PeriodoEnum;
import com.medic.Web.model.config.periodo.PeriodoTipo;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoResponseDTO(

        UUID id,
        PeriodoEnum descricao,
        PeriodoTipo tipo,
        Integer quantidade,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
