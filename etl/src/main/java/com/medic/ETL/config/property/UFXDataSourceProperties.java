package com.medic.ETL.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource.origem.ufx")
public class UFXDataSourceProperties {

    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
