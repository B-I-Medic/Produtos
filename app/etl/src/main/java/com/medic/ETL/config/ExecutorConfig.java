package com.medic.ETL.config;

import com.medic.ETL.config.property.ETLProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean(name = "etlExecutor")
    public Executor etlExecutor(ETLProperties properties) {
        return Executors.newFixedThreadPool(properties.getThreadPoolSize());
    }
}
