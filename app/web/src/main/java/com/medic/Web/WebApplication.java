package com.medic.Web;

import com.medic.Web.config.mail.MailProperties;
import com.medic.Web.config.properties.AnvisaEtlProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({MailProperties.class, AnvisaEtlProperties.class})
@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

}
