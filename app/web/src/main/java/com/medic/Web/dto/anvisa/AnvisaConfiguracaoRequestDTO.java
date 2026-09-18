package com.medic.Web.dto.anvisa;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AnvisaConfiguracaoRequestDTO(

        @NotNull @Min(1) @Max(1440) Integer retryCooldownMinutos
) {
}
