package com.medic.ETL.service.anvisa;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Component
public class AnvisaCsvReader {

    public void read(Path path, Set<String> requiredHeaders, Consumer<Map<String, String>> consumer) throws IOException {

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String header = reader.readLine();

            if (header == null || header.isBlank()) {
                throw new IllegalArgumentException("Arquivo CSV da Anvisa vazio: " + path.getFileName());
            }

            char separator = detectSeparator(header);

            Map<String, Integer> columns = mapColumns(parseLine(header, separator));

            Set<String> missingHeaders = requiredHeaders.stream()
                    .map(this::normalizeHeader)
                    .collect(java.util.stream.Collectors.toCollection(HashSet::new));

            missingHeaders.removeAll(columns.keySet());

            if (!missingHeaders.isEmpty()) {
                throw new IllegalArgumentException("Cabecalhos ausentes no arquivo " + path.getFileName() + ": " + missingHeaders);
            }

            String line;
            long rows = 0;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                Map<String, String> values = values(columns, parseLine(line, separator));
                consumer.accept(values);
                rows++;
            }

            if (rows == 0) {
                throw new IllegalArgumentException("Arquivo CSV da Anvisa sem registros: " + path.getFileName());
            }
        }
    }

    private Map<String, Integer> mapColumns(String[] header) {

        Map<String, Integer> columns = new HashMap<>();

        for (int index = 0; index < header.length; index++) {
            columns.put(normalizeHeader(header[index]), index);
        }

        return columns;
    }

    private Map<String, String> values(Map<String, Integer> columns, String[] row) {

        Map<String, String> values = new HashMap<>();

        columns.forEach((column, index) -> values.put(column, index < row.length ? row[index].trim() : ""));

        return values;
    }

    private char detectSeparator(String header) {

        long semicolons = header.chars().filter(character -> character == ';').count();
        long commas = header.chars().filter(character -> character == ',').count();
        long tabs = header.chars().filter(character -> character == '\t').count();

        if (semicolons >= commas && semicolons >= tabs) {
            return ';';
        }

        if (tabs >= commas) {
            return '\t';
        }

        return ',';
    }

    private String[] parseLine(String line, char separator) {

        var fields = new ArrayList<String>();
        var field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == separator && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }

        fields.add(field.toString());
        return fields.toArray(String[]::new);
    }

    private String normalizeHeader(String value) {

        return AnvisaTextoNormalizer.normalize(value);
    }

    public static String value(Map<String, String> values, String name) {

        return values.getOrDefault(AnvisaTextoNormalizer.normalize(name), "");
    }
}
