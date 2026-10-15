package com.medic.web.dto.config.periodo;

import com.medic.web.model.config.periodo.PeriodoEnum;
import com.medic.web.model.config.periodo.PeriodoTipo;

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
