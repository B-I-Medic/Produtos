package com.medic.Web.dto.web;

import java.time.Instant;

public record ErrorResponseDTO(

        Instant instant,
        String error,
        String message,
        String path
) {
}
