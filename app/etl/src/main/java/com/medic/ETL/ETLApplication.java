package com.medic.ETL;

import com.medic.ETL.config.property.ProdutoDataSourceProperties;
import com.medic.ETL.config.property.ETLProperties;
import com.medic.ETL.config.property.S00DataSourceProperties;
import com.medic.ETL.config.property.UFXDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		UFXDataSourceProperties.class,
		S00DataSourceProperties.class,
		ProdutoDataSourceProperties.class,
		ETLProperties.class
})
public class ETLApplication {

	public static void main(String[] args) {
		SpringApplication.run(ETLApplication.class, args);
	}

}
