package com.medic.web.dto.web;

import java.time.LocalDateTime;

public record ErrorResponseDTO(

        LocalDateTime dataHora,
        String erro,
        String descricao,
        String path
) {
}
