package com.clinica.mariana.restms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RestMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestMsApplication.class, args);
	}

}
