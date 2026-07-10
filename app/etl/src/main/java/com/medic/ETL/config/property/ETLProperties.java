package com.medic.ETL.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "etl")
public class ETLProperties {

    private int threadPoolSize;
}
