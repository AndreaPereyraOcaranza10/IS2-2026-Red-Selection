package com.tienda.zero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TiendaZeroApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiendaZeroApplication.class, args);
	}

}
