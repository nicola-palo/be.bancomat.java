package com.azienda.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.azienda.demo.config.DatabaseUrlInitializer;

@SpringBootApplication
public class AtmApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AtmApplication.class);
		app.addInitializers(new DatabaseUrlInitializer());
		app.run(args);
	}

}
