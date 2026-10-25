# Ejercicio e: OAuth 2.0

El perfil predeterminado `oauth2` incorpora un cliente OpenID Connect (login con
Authorization Code y PKCE S256) y un servidor de recursos que valida access tokens
JWT del mismo proveedor. El navegador conserva la autenticación en sesión; los
clientes HTTP envían `Authorization: Bearer <access_token>`. El JWT propio anterior
queda disponible únicamente al iniciar con el perfil `jwt`.

## Ejecutar en desarrollo (PowerShell)

Requisitos: Java 17 o superior, Docker Compose y MySQL con la base `mascotas_db`
de la aplicación. Revisar las credenciales de MySQL en `application.properties`;
se pueden reemplazar mediante `SPRING_DATASOURCE_USERNAME` y
`SPRING_DATASOURCE_PASSWORD`.

```powershell
$env:KEYCLOAK_ADMIN_PASSWORD = '<elegir-una-clave-admin>'
$env:OAUTH2_CLIENT_SECRET = '<elegir-un-secreto-largo>'
docker compose up -d
# Esperar a que http://localhost:8081 esté disponible.
.\mvnw.cmd spring-boot:run
```

El realm `mascotas` y el cliente confidencial `mascotas-web` se importan al primer
inicio. El secreto se toma de la variable de entorno en ambos procesos. La base
de Keycloak se conserva en un volumen; cambiar el JSON o el secreto del entorno
no modifica un realm ya importado: actualizar el cliente desde la consola.

## Vincular una cuenta

1. Registrar el usuario local en `http://localhost:8080/registro` (requiere las
   zonas existentes de la aplicación).
2. Entrar a `http://localhost:8081` como `admin`, seleccionar el realm `mascotas`,
   crear el usuario y asignarle una contraseña desde Credentials.
3. Copiar el ID de ese usuario de Keycloak (el claim `sub`) y vincularlo al ID del
   usuario local en MySQL:

   ```sql
   UPDATE usuario SET oauth2_subject = '<ID-de-Keycloak>' WHERE id = '<ID-local>';
   ```

4. Abrir `http://localhost:8080/login` y pulsar **Ingresar con OAuth 2.0**.

La vinculación es una tarea administrativa. No se vinculan cuentas automáticamente
por correo ni se importan roles desde claims del cliente. `oauth2_subject` es único;
se utilizan los roles locales `USUARIO_REGISTRADO` y `ADMIN`. Si se cambia de emisor,
se deben revisar y reemplazar todas las vinculaciones antes de habilitar accesos.
Hibernate agrega la columna al iniciar con el `ddl-auto=update` existente.

## Servidor y cliente HTTP

Se verifican firma (JWKS), emisor, vencimiento y audiencia `mascotas-api` mediante
Spring Security. Usar un **access token**, no un ID token. Para probar con un
cliente externo, registrarlo en el realm con Authorization Code + PKCE, su URI de
retorno exacta y un audience mapper que incluya `mascotas-api` en el access token.
El cliente web incluido también emite esa audiencia. No se habilita password grant.

```powershell
$headers = @{ Authorization = "Bearer <access_token>" }
Invoke-RestMethod http://localhost:8080/api/me -Headers $headers
```

`/api/**` y las solicitudes con Authorization requieren Bearer válido; una sesión
del navegador no los autentica. Los controladores HTML anteriores también admiten
Bearer y reciben el DTO del usuario para mantener su compatibilidad; este DTO en
sesión no acredita identidad. Las rutas privadas requieren autenticación y
`/reportes/**` exige `ADMIN`. Cuentas sin vínculo o dadas de baja no acceden; los
roles y el estado se consultan en cada solicitud. La edición del perfil solo
permite modificar la cuenta autenticada.

Los formularios y el cierre de sesión (`POST /logout`) conservan CSRF. La cadena
Bearer no usa autenticación por cookies y no requiere CSRF. Cerrar sesión elimina
la sesión de esta aplicación; la sesión del proveedor continúa activa. En modo
OAuth2 no se aceptan `/auth/login`, `/logincheck` ni cookies JWT del modo anterior.

Variables opcionales: `OAUTH2_ISSUER_URI`, `OAUTH2_CLIENT_ID`, `OAUTH2_AUDIENCE`.
La URL de retorno registrada por defecto es
`http://localhost:8080/login/oauth2/code/mascotas`. Para desplegar, configurar las
URLs HTTPS y el proveedor correspondiente; el Compose usa `start-dev` para uso local.

Para usar explícitamente el ejercicio JWT anterior:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'jwt'
.\mvnw.cmd spring-boot:run
```

Referencias: [OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html),
[Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html),
[Keycloak Docker](https://www.keycloak.org/getting-started/getting-started-docker).

## Verificación

Ejecutar `.\mvnw.cmd test`. Las pruebas usan H2 y un proveedor simulado; cubren
tokens firmados RSA, firma incorrecta, vencimiento, emisor, audiencia, permisos,
cuentas deshabilitadas, PKCE/state, sesión, CSRF y compatibilidad del perfil JWT.
La prueba del contexto completo no necesita MySQL ni Keycloak.
