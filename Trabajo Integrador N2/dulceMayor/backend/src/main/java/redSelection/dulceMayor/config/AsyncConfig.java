package redSelection.dulceMayor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Habilita @Async: los correos (ej. la factura de BK-08) se envían en segundo plano, sin demorar la respuesta. */
@Configuration
@EnableAsync
public class AsyncConfig { }
