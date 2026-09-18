package com.medic.ETL.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "anvisa")
public class AnvisaProperties {

    private String produtosUrl = "https://dados.anvisa.gov.br/dados/TA_PRODUTO_SAUDE_SITE.csv";
    private String modelosUrl = "https://dados.anvisa.gov.br/dados/tmp/TA_PRODUTO_SAUDE_MODELO.csv";

    private String internalApiKey;
    private WorkerProperties worker = new WorkerProperties();
    private TlsProperties tls = new TlsProperties();

    @Getter
    @Setter
    public static class WorkerProperties {

        private long leaseSeconds;
    }

    @Getter
    @Setter
    public static class TlsProperties {

        private String trustStore;
        private String trustStorePassword;
        private String trustStoreType;
    }
}
