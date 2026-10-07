# Ejercicio C - Cliente

Aplicación cliente (Spring Boot + Thymeleaf) que consume la API REST del servidor `ejercicio-c`
usando **RestTemplate** y **DTOs**, y muestra los datos en un sitio HTML.

## Cómo ejecutarlo

1. Levantar MySQL y el **servidor** (`ejercicio-c`), que escucha en el puerto `8080`.
2. Levantar este **cliente** (`ClienteApplication`), que escucha en el puerto `8081`.
3. Abrir <http://localhost:8081>.

Como el servidor usa `ddl-auto=create-drop`, la base se borra en cada arranque:
hay que cargar al menos una localidad (menú *Localidades*) antes de crear personas.
Los libros se cargan desde el detalle de cada persona, eligiendo autores del menú *Autores*.

La URL del servidor se configura en `src/main/resources/application.properties` (`api.base-url`).

## Estructura

| Paquete | Rol |
|---|---|
| `dto` | Objetos que viajan entre cliente y servidor y que usan las vistas (`PersonaDTO`, `DomicilioDTO`, `LocalidadDTO`, `LibroDTO`, `AutorDTO`) |
| `service` | Llamadas HTTP al servidor con `RestTemplate` |
| `controller` | Controladores MVC que arman el modelo para las vistas |
| `config` | Bean de `RestTemplate` |
| `templates` | Vistas HTML con Thymeleaf |
