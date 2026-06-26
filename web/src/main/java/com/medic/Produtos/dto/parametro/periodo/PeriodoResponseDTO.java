package com.medic.Produtos.dto.parametro.periodo;

import com.medic.Produtos.model.parametro.periodo.PeriodoEnum;

import java.time.LocalDate;
import java.util.UUID;

public record PeriodoResponseDTO(

        UUID id,
        PeriodoEnum descricao,
        LocalDate dataInicial,
        LocalDate dataFinal
) {
}
