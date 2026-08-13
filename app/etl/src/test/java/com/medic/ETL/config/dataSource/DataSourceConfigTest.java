package com.medic.ETL.config.dataSource;

import com.medic.ETL.config.property.ProdutoDataSourceProperties;
import com.medic.ETL.config.property.S00DataSourceProperties;
import com.medic.ETL.config.property.UFXDataSourceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataSourceConfigTest {

    private final DataSourceConfig config = new DataSourceConfig();

    @Test
    void shouldConfigurePostgresDatasourceWithBusinessTimezone() {
        ProdutoDataSourceProperties properties = new ProdutoDataSourceProperties();
        properties.setDriverClassName("org.postgresql.Driver");
        properties.setUrl("jdbc:postgresql://localhost:5432/produto");
        properties.setUsername("produtoetl");
        properties.setPassword("secret");

        var dataSource = assertInstanceOf(DriverManagerDataSource.class, config.produtoDataSource(properties));

        assertEquals("jdbc:postgresql://localhost:5432/produto", dataSource.getUrl());
        assertEquals("produtoetl", dataSource.getUsername());
        assertEquals("-c TimeZone=America/Sao_Paulo", dataSource.getConnectionProperties().getProperty("options"));
    }

    @Test
    void shouldConfigureDbmakerDatasourcesAndTemplates() {
        UFXDataSourceProperties ufxProperties = new UFXDataSourceProperties();
        ufxProperties.setUrl("jdbc:dbmaker:ufx");
        ufxProperties.setUsername("ufx");
        ufxProperties.setPassword("secret");
        S00DataSourceProperties s00Properties = new S00DataSourceProperties();
        s00Properties.setUrl("jdbc:dbmaker:s00");
        s00Properties.setUsername("s00");
        s00Properties.setPassword("secret");

        var ufxDataSource = assertInstanceOf(DriverManagerDataSource.class, config.ufxDataSource(ufxProperties));
        var s00DataSource = assertInstanceOf(DriverManagerDataSource.class, config.s00DataSource(s00Properties));
        JdbcTemplate ufxTemplate = config.ufxJbcTemplate(ufxDataSource);
        JdbcTemplate s00Template = config.s00JbcTemplate(s00DataSource);

        assertEquals("jdbc:dbmaker:ufx", ufxDataSource.getUrl());
        assertEquals("jdbc:dbmaker:s00", s00DataSource.getUrl());
        assertSame(ufxDataSource, ufxTemplate.getDataSource());
        assertSame(s00DataSource, s00Template.getDataSource());
    }
}
