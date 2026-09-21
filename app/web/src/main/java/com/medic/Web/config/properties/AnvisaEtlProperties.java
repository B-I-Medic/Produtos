package com.medic.Web.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "anvisa.etl")
public class AnvisaEtlProperties {

    private String baseUrl;
    private String apiKey;
}
