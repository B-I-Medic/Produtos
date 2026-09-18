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

class AnvisaCsvReaderCoverageTest {

    private final AnvisaCsvReader reader = new AnvisaCsvReader();

    @Test
    void shouldReadCommaAndTabSeparatedQuotedRowsAndFillMissingColumns(@TempDir Path tempDir) throws Exception {

        Path commaCsv = tempDir.resolve("comma.csv");
        Files.writeString(commaCsv,
                "A,B,C\n\"um,dois\",\"aspas \"\"internas\"\"\n",
                StandardCharsets.UTF_8);
        List<Map<String, String>> commaRows = new ArrayList<>();

        reader.read(commaCsv, Set.of("A", "B", "C"), commaRows::add);

        assertEquals("um,dois", commaRows.getFirst().get("A"));
        assertEquals("aspas \"internas\"", commaRows.getFirst().get("B"));
        assertEquals("", commaRows.getFirst().get("C"));

        Path tabCsv = tempDir.resolve("tab.csv");
        Files.writeString(tabCsv, "A\tB\n1\t2\n\n", StandardCharsets.UTF_8);
        List<Map<String, String>> tabRows = new ArrayList<>();
        reader.read(tabCsv, Set.of("A", "B"), tabRows::add);
        assertEquals("2", tabRows.getFirst().get("B"));
    }

    @Test
    void shouldRejectEmptyFilesMissingHeadersAndFilesWithoutRecords(@TempDir Path tempDir) throws Exception {

        Path empty = tempDir.resolve("empty.csv");
        Files.writeString(empty, "", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> reader.read(empty, Set.of("A"), ignored -> {}));

        Path missingHeader = tempDir.resolve("missing.csv");
        Files.writeString(missingHeader, "A\n1\n", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> reader.read(missingHeader, Set.of("B"), ignored -> {}));

        Path noRows = tempDir.resolve("no-rows.csv");
        Files.writeString(noRows, "A;B\n\n", StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> reader.read(noRows, Set.of("A", "B"), ignored -> {}));
    }

    @Test
    void shouldNormalizeStaticValuesAndNullInput() {

        assertThrows(NullPointerException.class, () -> AnvisaCsvReader.value(null, "A"));
        assertEquals("valor", AnvisaCsvReader.value(Map.of("A", "valor"), "á"));
    }
}
