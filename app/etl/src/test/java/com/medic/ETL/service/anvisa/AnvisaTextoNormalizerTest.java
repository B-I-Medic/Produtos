package com.medic.ETL.service.anvisa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnvisaTextoNormalizerTest {

    @Test
    void shouldRemoveAccentsPunctuationAndWhitespace() {

        assertEquals("NUMEROREGISTRO123", AnvisaTextoNormalizer.normalize(" Número-Registro 123 "));
    }

    @Test
    void shouldNormalizeNullAsAnEmptyString() {

        assertEquals("", AnvisaTextoNormalizer.normalize(null));
    }
}
