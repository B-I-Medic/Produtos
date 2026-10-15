package com.medic.web;

import com.medic.web.config.mail.MailProperties;
import com.medic.web.config.properties.ApplicationProperties;
import com.medic.web.config.properties.AnvisaEtlProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({MailProperties.class, AnvisaEtlProperties.class, ApplicationProperties.class})
@SpringBootApplication
public class WebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

}
