package com.RedSelection.videojuegos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VideojuegosApplication {

	public static void main(String[] args) {

		SpringApplication.run(VideojuegosApplication.class, args);
	}

}
