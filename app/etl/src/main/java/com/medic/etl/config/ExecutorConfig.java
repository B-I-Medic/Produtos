package com.medic.etl.config;

import com.medic.etl.config.property.ETLProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public Executor etlExecutor(ETLProperties properties) {
        return Executors.newFixedThreadPool(properties.getThreadPoolSize());
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService anvisaExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
