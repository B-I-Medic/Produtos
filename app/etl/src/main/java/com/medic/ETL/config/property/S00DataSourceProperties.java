package com.medic.ETL.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.datasource.origem.s00")
public class S00DataSourceProperties {

    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
