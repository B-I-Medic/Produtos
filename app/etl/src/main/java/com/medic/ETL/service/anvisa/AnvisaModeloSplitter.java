package com.medic.ETL.service.anvisa;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AnvisaModeloSplitter {

    private static final Pattern POSSIBLE_MODEL_AFTER_COMMA = Pattern.compile(
            ",\\s*(?=([A-Za-z][A-Za-z0-9./_-]{2,}|[A-Z0-9]+(?:[-/.][A-Z0-9]+)+)(?:\\s|$))"
    );

    public List<String> split(String value) {

        if (value == null || value.isBlank()) {
            return List.of();
        }

        List<String> parts = new ArrayList<>();
        Matcher matcher = POSSIBLE_MODEL_AFTER_COMMA.matcher(value);
        int start = 0;

        while (matcher.find()) {
            String part = value.substring(start, matcher.start()).trim();

            if (!part.isBlank()) {
                parts.add(part);
            }

            start = matcher.end();
        }

        String lastPart = value.substring(start).trim();

        if (!lastPart.isBlank()) {
            parts.add(lastPart);
        }

        return parts.isEmpty() ? List.of(value.trim()) : parts;
    }
}
