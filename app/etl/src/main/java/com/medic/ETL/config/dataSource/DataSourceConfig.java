package com.medic.ETL.config.dataSource;

import com.medic.ETL.config.property.ProdutoDataSourceProperties;
import com.medic.ETL.config.property.S00DataSourceProperties;
import com.medic.ETL.config.property.UFXDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean(name = "UFXDataSource")
    public DataSource ufxDataSource(UFXDataSourceProperties properties) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("dbmaker.sql.type4.Driver");
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());

        return dataSource;
    }

    @Bean(name = "UFXJdbcTemplate")
    public JdbcTemplate ufxJbcTemplate(@Qualifier("UFXDataSource") DataSource origemDataSource) {

        return new JdbcTemplate(origemDataSource);
    }

    @Bean(name = "S00DataSource")
    public DataSource s00DataSource(S00DataSourceProperties properties) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("dbmaker.sql.type4.Driver");
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());

        return dataSource;
    }

    @Bean(name = "S00JdbcTemplate")
    public JdbcTemplate s00JbcTemplate(@Qualifier("S00DataSource") DataSource origemDataSource) {

        return new JdbcTemplate(origemDataSource);
    }

    @Primary
    @Bean(name = "pgDataSource")
    public DataSource produtoDataSource(ProdutoDataSourceProperties properties) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());

        return dataSource;
    }

    @Bean(name = "pgJdbcTemplate")
    public JdbcTemplate produtoJbcTemplate(@Qualifier("pgDataSource") DataSource origemDataSource) {

        return new JdbcTemplate(origemDataSource);
    }
}
