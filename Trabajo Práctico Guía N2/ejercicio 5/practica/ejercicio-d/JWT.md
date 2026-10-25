# Seguridad JWT en mascotas

Se reutilizan los usuarios de MySQL, las claves BCrypt y los roles existentes.
Se incorporan JJWT 0.11.5, el servicio de tokens y el filtro de autenticación,
siguiendo el enfoque de ejercicio-c/parte-a, adaptado a las vistas de mascotas.

## Navegador

Iniciar sesión desde `/login` como antes. El formulario `/logincheck` genera un
JWT firmado con HS256 en la cookie `mascotasJwt` (HttpOnly, SameSite=Lax y Secure
cuando se usa HTTPS). El token vence en una hora; con «Recordarme», dura dos días.
Salir envía POST `/logout` con CSRF y elimina la cookie.

Spring Security no guarda la autenticación en la sesión (`STATELESS`). Cada
petición valida nuevamente el JWT y consulta el usuario y sus roles actuales.
La sesión se conserva para CSRF y los datos `usuariosession` que utilizan los
controladores y las vistas existentes; esos datos por sí solos no autentican.
Los formularios mantienen la protección CSRF.

## Postman

1. Enviar POST `http://localhost:8080/auth/login`, con `Content-Type: application/json`:

   ```json
   {"username":"mail-del-usuario", "password":"clave-del-usuario"}
   ```

2. Copiar el campo `token` de la respuesta y enviar, por ejemplo, GET
   `http://localhost:8080/inicio` con `Authorization: Bearer <token>`.
   Las rutas existentes siguen devolviendo sus vistas HTML.
3. Un token inválido, vencido o de un usuario deshabilitado devuelve 401.
   `/reportes/**` requiere ADMIN; sin ese rol devuelve 403.

El login JSON devuelve únicamente el token, sin establecer una cookie.
Las peticiones con Bearer no necesitan CSRF. El logout elimina la cookie del
navegador; un token copiado sigue siendo válido hasta su vencimiento.
Si se modifica el mail del perfil, volver a iniciar sesión, porque el mail es
el subject del token.

## Configuración y pruebas

En `src/main/resources/application.properties`, `jwt.expiration-seconds` define
la duración normal. Opcionalmente, establecer `JWT_SECRET` con una clave aleatoria
de al menos 32 bytes codificada en Base64. Si no se configura, se genera una clave
al arrancar y los tokens anteriores dejan de funcionar al reiniciar.

No editar `target/classes/application.properties`: Maven lo genera desde `src`.

Pruebas de JWT sin depender de MySQL:

```powershell
.\mvnw.cmd -Dtest=JwtSecurityTests test
```

La prueba original `MascotasApplicationTests` carga toda la aplicación y requiere
la base MySQL configurada en el proyecto.
