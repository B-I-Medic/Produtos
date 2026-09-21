package com.medic.ETL.service.anvisa;

import java.text.Normalizer;
import java.util.Locale;

public final class AnvisaTextoNormalizer {

    private AnvisaTextoNormalizer() {
    }

    public static String normalize(String value) {

        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutAccents
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }
}
