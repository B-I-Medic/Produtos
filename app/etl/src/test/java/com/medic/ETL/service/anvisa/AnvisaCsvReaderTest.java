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

class AnvisaCsvReaderTest {

    private final AnvisaCsvReader reader = new AnvisaCsvReader();

    @Test
    void shouldReadLatin1CsvWithAccentedCharacters(@TempDir Path tempDir) throws Exception {

        Path csv = tempDir.resolve("produtos.csv");
        Files.writeString(csv,
                "NUMERO_REGISTRO_CADASTRO;NOME_TECNICO\n123;Coração\n",
                StandardCharsets.ISO_8859_1);

        List<Map<String, String>> rows = new ArrayList<>();

        reader.read(csv,
                Set.of("NUMERO_REGISTRO_CADASTRO", "NOME_TECNICO"),
                rows::add);

        assertEquals(1, rows.size());
        assertEquals("Coração", AnvisaCsvReader.value(rows.getFirst(), "NOME_TECNICO"));
    }
}
