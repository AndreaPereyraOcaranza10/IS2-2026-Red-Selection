package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * PROYECTO: DEMO ABM - PARADIGMAS DE PROGRAMACIÓN
 * 
 * OBJETIVO DEL PROYECTO:
 * Esta aplicación sirve como ejercicio didáctico para comprender el intercambio 
 * de información entre la interfaz de usuario (Vistas en HTML/Thymeleaf) y el 
 * backend en Spring Boot (Modelo y Controlador), usando dos estrategias distintas
 * para capturar datos HTTP.
 * 
 * UTILIDAD DE LAS VISTAS DEL SISTEMA:
 * 
 * 1. PANTALLA PRINCIPAL (DEMO ABM):
 *    - Presenta el marco teórico del ejercicio explicativo.
 *    - Detalla la diferencia pedagógica entre las dos formas de comunicación MVC:
 *      * ABM País: Utiliza variables de ruta (@PathVariable). Las peticiones envían
 *        los datos dentro de la propia URL (ej. '/pais/baja/123456').
 *      * ABM Nacionalidad: Utiliza parámetros de consulta (@RequestParam). Las
 *        peticiones pasan los datos como Query Params (ej. '/nacionalidad/baja?id=123456').
 * 
 * 2. VISTAS DE TABLAS / LISTADOS (Lista País y Lista Nacionalidad):
 *    - Muestran el listado dinámico de registros recuperados desde MySQL.
 *    - Incluyen una botonera de acciones (Ver, Editar, Eliminar y Agregar con el botón '+').
 *    - Permiten observar cómo la vista construye las URLs según el tipo de ABM para 
 *      disparar las peticiones GET o POST hacia los controladores.
 * 
 * 3. FORMULARIOS DE ALTA, CONSULTA Y MODIFICACIÓN:
 *    - Muestran dos enfoques para la captura de datos en inputs y renderizado de mensajes
 *      de confirmación (ej. 'La acción fue realizada correctamente').
 */
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		// Inicia el contexto de Spring Boot, conecta la base de datos MySQL 
		// y despliega las vistas interactivas del ABM en el servidor Tomcat.
		SpringApplication.run(DemoApplication.class, args);
	}

}
