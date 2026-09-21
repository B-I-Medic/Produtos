package com.medic.Web.dto.config.periodo;

import com.medic.Web.model.config.periodo.PeriodoTipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PeriodoRequestDTO(

        @NotNull(message = "O tipo do período é obrigatório")
        PeriodoTipo tipo,

        @NotNull(message = "A quantidade do período é obrigatória")
        @Positive(message = "A quantidade do período deve ser maior que zero")
        Integer quantidade

) {
}
