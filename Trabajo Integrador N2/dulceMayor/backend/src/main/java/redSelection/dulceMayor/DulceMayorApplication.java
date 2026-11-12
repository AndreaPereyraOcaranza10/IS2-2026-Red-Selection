package redSelection.dulceMayor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DulceMayorApplication {
	public static void main(String[] args) {
		SpringApplication.run(DulceMayorApplication.class, args);
	}
}
