package com.medic.ETL.config;

import com.medic.ETL.config.property.ETLProperties;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorConfigTest {

    @Test
    void shouldCreateConfiguredExecutors() {

        ETLProperties properties = new ETLProperties();
        properties.setThreadPoolSize(2);
        ExecutorConfig config = new ExecutorConfig();

        Executor etlExecutor = config.etlExecutor(properties);
        ExecutorService anvisaExecutor = config.anvisaExecutor();

        assertNotNull(etlExecutor);
        assertTrue(etlExecutor instanceof ExecutorService);
        assertNotNull(anvisaExecutor);
        anvisaExecutor.shutdownNow();
        ((ExecutorService) etlExecutor).shutdownNow();
    }
}
