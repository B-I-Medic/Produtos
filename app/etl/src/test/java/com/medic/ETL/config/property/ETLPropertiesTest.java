package com.medic.ETL.config.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ETLPropertiesTest {

    @Test
    void shouldExposeTheThreadPoolSize() {

        ETLProperties properties = new ETLProperties();
        properties.setThreadPoolSize(7);

        assertEquals(7, properties.getThreadPoolSize());
    }
}
