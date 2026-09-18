package com.medic.ETL.service.anvisa;

import com.medic.ETL.config.property.AnvisaProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

@Service
public class AnvisaDownloadService {

    private final AnvisaProperties properties;
    private final HttpClient httpClient;

    public AnvisaDownloadService(AnvisaProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .sslContext(createSslContext(properties.getTls()))
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMinutes(2))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private static SSLContext createSslContext(AnvisaProperties.TlsProperties tlsProperties) {

        if (tlsProperties == null) {
            throw new IllegalStateException("As propriedades TLS da Anvisa não foram configuradas.");
        }

        String trustStorePath = required(tlsProperties.getTrustStore(), "anvisa.tls.trust-store");
        String trustStorePassword = required(tlsProperties.getTrustStorePassword(), "anvisa.tls.trust-store-password");
        String trustStoreType = tlsProperties.getTrustStoreType();

        if (trustStoreType == null || trustStoreType.isBlank()) {
            trustStoreType = "PKCS12";
        }

        try (InputStream inputStream = Files.newInputStream(Path.of(trustStorePath))) {

            KeyStore trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(inputStream, trustStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;

        } catch (IOException | GeneralSecurityException exception) {

            throw new IllegalStateException(
                    "Não foi possível carregar o truststore TLS da Anvisa em '" + trustStorePath + "'.",
                    exception
            );
        }
    }

    private static String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("A propriedade '" + propertyName + "' é obrigatória.");
        }
        return value;
    }

    public ArquivosAnvisa baixar() throws IOException, InterruptedException {

        Path produtos = download(properties.getProdutosUrl(), "anvisa-produtos-");

        try {
            Path modelos = download(properties.getModelosUrl(), "anvisa-modelos-");
            return new ArquivosAnvisa(produtos, modelos);
        } catch (IOException | InterruptedException exception) {
            Files.deleteIfExists(produtos);
            throw exception;
        }
    }

    private Path download(String url, String prefix) throws IOException, InterruptedException {

        Path destination = Files.createTempFile(prefix, ".csv");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(10))
                .header("Accept", "text/csv,application/octet-stream;q=0.9,*/*;q=0.8")
                .GET()
                .build();

        HttpResponse<Path> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofFile(destination)
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            Files.deleteIfExists(destination);
            throw new IOException("Falha ao baixar arquivo da Anvisa. HTTP " + response.statusCode() + " - " + url);
        }

        if (Files.size(destination) == 0) {
            Files.deleteIfExists(destination);
            throw new IOException("Arquivo vazio retornado pela Anvisa: " + url);
        }

        return destination;
    }

    public record ArquivosAnvisa(Path produtos, Path modelos) {
    }
}
