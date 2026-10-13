package redselection.ejercicio_c;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PersonaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonaApplication.class, args);
	}

}
