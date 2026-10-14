package com.medic.ETL.service.anvisa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnvisaCsvReaderEdgeCasesTest {

    private final AnvisaCsvReader reader = new AnvisaCsvReader();

    @Test
    void shouldRejectMissingAndBlankHeaderRows(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("missing-header.csv");
        Files.writeString(csv, "", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> reader.read(csv, Set.of("CODIGO"), ignored -> {}));

        Files.writeString(csv, "   \n", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> reader.read(csv, Set.of("CODIGO"), ignored -> {}));
    }

    @Test
    void shouldDecodeEscapedQuotesInQuotedFields(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("escaped-quotes.csv");
        Files.writeString(csv, "CODIGO;NOME\n1;\"Material \"\"A\"\"\"\n", StandardCharsets.UTF_8);
        List<Map<String, String>> rows = new ArrayList<>();

        reader.read(csv, Set.of("CODIGO", "NOME"), rows::add);

        assertEquals("Material \"A\"", AnvisaCsvReader.value(rows.getFirst(), "NOME"));
    }
}
