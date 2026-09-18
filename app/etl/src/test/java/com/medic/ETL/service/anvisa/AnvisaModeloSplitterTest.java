package com.medic.ETL.service.anvisa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnvisaModeloSplitterTest {

    private final AnvisaModeloSplitter splitter = new AnvisaModeloSplitter();

    @Test
    void shouldKeepCommasInsideDimensionDescription() {

        String value = "SIJSTIM1002010C Kit Canula - 100 mm de comprimento, 20 gauge, 10 mm de ponta curva";

        assertEquals(List.of(value), splitter.split(value));
    }

    @Test
    void shouldSplitConcatenatedModelsWhenNextPartStartsWithModelCode() {

        String value = "BLOCK1002010S Kit canula, BLOCK1002205S Kit canula, BLOCK1502010C Kit canula";

        assertEquals(3, splitter.split(value).size());
        assertEquals("BLOCK1002010S Kit canula", splitter.split(value).get(0));
        assertEquals("BLOCK1502010C Kit canula", splitter.split(value).get(2));
    }

    @Test
    void shouldHandleEmptyValuesAndValuesWithoutRecognizableModelSeparators() {

        assertEquals(List.of(), splitter.split(null));
        assertEquals(List.of(), splitter.split("  "));
        assertEquals(List.of(", ,"), splitter.split(", ,"));
    }
}
