package redselection.ejercicio_c.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/*
========   PROPIEDADES PARA SCHEDULED ============================
- fixedRate: ejecuta a intervalos fijos desde el inicio de cada ejecución, no le importa si la tarea ya terminó o no la vuelve a ejecutar
- fixedDelay: ejecuta a intervalos fijos desde el final de cada ejecucion, espera a que termine la ejecución anterior y vuelve a ejecutar
- initialDelay: retrasa la primera ejecución de la tarea
- cron: ejecuta según una expresión CRON, CRON = especie de nomenclatura para expresar periodos de tiempo (se genera con crongenerator)
- zone: especifica la zona horaria para una expresion cron

===== OTRAS NOTACIONES =====
@yearly --> Scheduled(cron = "@yearly", zone = "America/Argentina")
@monthly
@weakly
*/

@Component
public class ScheduleTask {

    //Se ejecuta esta tarea cada 5 segundos, siempre en milisegundos
    @Scheduled(fixedRate = 5000)
    public void scheduleMessage() {
        System.out.println("Mensaje programado");
    }

    //zonas horarias de java
    /*public static void main(String[] args) {
        ZoneId.getAvailableZoneIds().forEach(System.out::println);
    }*/
}
