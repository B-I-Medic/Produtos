package com.medic.ETL.service.anvisa;

import com.medic.ETL.config.property.AnvisaProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnvisaDownloadServiceTest {

    @Test
    void shouldDownloadBothFilesUsingTheConfiguredTrustStore(@TempDir Path tempDir) throws Exception {

        HttpServer server = server(200, "csv-content");
        server.start();
        try {
            AnvisaProperties properties = properties(tempDir, server, "modelos");
            AnvisaDownloadService.ArquivosAnvisa files = new AnvisaDownloadService(properties).baixar();

            assertTrue(Files.size(files.produtos()) > 0);
            assertTrue(Files.size(files.modelos()) > 0);
            Files.deleteIfExists(files.produtos());
            Files.deleteIfExists(files.modelos());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRejectNonSuccessfulResponses(@TempDir Path tempDir) throws Exception {

        HttpServer server = server(503, "unavailable");
        server.start();
        try {
            assertThrows(java.io.IOException.class,
                    () -> new AnvisaDownloadService(properties(tempDir, server, "modelos")).baixar());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldDeleteTheFirstFileWhenTheSecondDownloadIsEmpty(@TempDir Path tempDir) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/produtos", exchange -> respond(exchange, 200, "produtos"));
        server.createContext("/modelos", exchange -> respond(exchange, 200, ""));
        server.start();
        try {
            assertThrows(java.io.IOException.class,
                    () -> new AnvisaDownloadService(properties(tempDir, server, "modelos")).baixar());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldRejectMissingTlsProperties(@TempDir Path tempDir) {

        AnvisaProperties noTls = new AnvisaProperties();
        noTls.setTls(null);
        assertThrows(IllegalStateException.class, () -> new AnvisaDownloadService(noTls));

        AnvisaProperties noTrustStore = new AnvisaProperties();
        noTrustStore.getTls().setTrustStore(" ");
        noTrustStore.getTls().setTrustStorePassword("password");
        assertThrows(IllegalStateException.class, () -> new AnvisaDownloadService(noTrustStore));

        AnvisaProperties invalidStore = new AnvisaProperties();
        invalidStore.getTls().setTrustStore(tempDir.resolve("missing.p12").toString());
        invalidStore.getTls().setTrustStorePassword("password");
        assertThrows(IllegalStateException.class, () -> new AnvisaDownloadService(invalidStore));
    }

    private AnvisaProperties properties(Path tempDir, HttpServer server, String modelsPath) throws Exception {

        Path trustStore = tempDir.resolve("truststore.p12");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, "password".toCharArray());
        try (var output = Files.newOutputStream(trustStore)) {
            keyStore.store(output, "password".toCharArray());
        }

        AnvisaProperties properties = new AnvisaProperties();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        properties.setProdutosUrl(baseUrl + "/produtos");
        properties.setModelosUrl(baseUrl + "/" + modelsPath);
        properties.getTls().setTrustStore(trustStore.toString());
        properties.getTls().setTrustStorePassword("password");
        properties.getTls().setTrustStoreType(null);
        return properties;
    }

    private HttpServer server(int status, String body) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/produtos", exchange -> respond(exchange, status, body));
        server.createContext("/modelos", exchange -> respond(exchange, status, body));
        return server;
    }

    private void respond(HttpExchange exchange, int status, String body) throws java.io.IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
