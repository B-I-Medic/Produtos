package com.medic.etl;

import com.medic.etl.config.property.ProdutoDataSourceProperties;
import com.medic.etl.config.property.ETLProperties;
import com.medic.etl.config.property.S00DataSourceProperties;
import com.medic.etl.config.property.UFXDataSourceProperties;
import com.medic.etl.config.property.AnvisaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		UFXDataSourceProperties.class,
		S00DataSourceProperties.class,
		ProdutoDataSourceProperties.class,
		ETLProperties.class,
		AnvisaProperties.class
})
public class ETLApplication {

	public static void main(String[] args) {
		SpringApplication.run(ETLApplication.class, args);
	}

}
