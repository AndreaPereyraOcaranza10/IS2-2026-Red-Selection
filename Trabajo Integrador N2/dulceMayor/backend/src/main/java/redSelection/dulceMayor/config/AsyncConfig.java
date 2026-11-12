package redSelection.dulceMayor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** Habilita @Async: los correos (ej. la factura de BK-08) se envían en segundo plano, sin demorar la respuesta. */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "newsletterTaskScheduler")
    ThreadPoolTaskScheduler newsletterTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("newsletter-");
        return scheduler;
    }
}
