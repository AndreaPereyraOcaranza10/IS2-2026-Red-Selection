# Atlas · Mi cuenta personal

Aplicación para usuarios finales. Después de iniciar sesión, el menú contiene únicamente:

- **Mi Perfil**: datos de cuenta, nombre, apellido, DNI, fecha de nacimiento y dirección propia.
- **Cambiar Contraseña**: formulario independiente con validación y cierre de las sesiones anteriores.
- **Cerrar Sesión**.

El inicio y las rutas antiguas redirigen a `/mi-perfil`. Se eliminaron Dashboard y los ABM de países, provincias, departamentos, localidades, direcciones y personas, incluidos sus endpoints HTTP. No se exponen listados de otras personas ni de sus domicilios.

## Ejecutar con Docker

Con Docker iniciado, desde este directorio:

```powershell
Copy-Item .env.example .env
```

Completá `DB_PASSWORD` y `JWT_SECRET` con valores aleatorios diferentes. JWT_SECRET requiere al menos 32 bytes. Para generar cada secreto podés usar `openssl rand -hex 32` o `[Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')` en PowerShell.

```sh
docker compose up -d --build
docker compose ps
```

Abrí **http://localhost** y registrá tu cuenta, o ingresá con Google si lo configuraste. Todas las cuentas nuevas son USER. No hay un paso de creación de administrador.

Swagger: `http://localhost/swagger-ui/index.html`. Salud: `http://localhost/actuator/health`.

**Si ya tenías la aplicación ejecutándose, reiniciá el backend y recargá el frontend.** En Docker, el comando anterior reconstruye y reinicia los servicios. En el IDE, detené y volvé a ejecutar `AbmApplication` con tus variables de entorno. No borres la base ni los volúmenes para actualizar.

## Datos geográficos precargados

El script está en:

[`backend/src/main/resources/db/migration/V2__catalogo_geografico.sql`](backend/src/main/resources/db/migration/V2__catalogo_geografico.sql).

Flyway lo ejecuta automáticamente tanto en una base nueva como al actualizar una existente. Incluye un catálogo inicial de ejemplo, **no un padrón completo del país**:

| País | Provincia | Departamento | Localidad | CP |
| --- | --- | --- | --- | --- |
| Argentina | Mendoza | Capital | Ciudad de Mendoza | 5500 |
| Argentina | Buenos Aires | La Plata | La Plata | 1900 |
| Argentina | Córdoba | Capital | Córdoba | 5000 |
| Argentina | Santa Fe | Rosario | Rosario | 2000 |

El script puede repetirse sin duplicar registros; busca por nombre y por su padre, sin asumir ids fijos. Conserva los datos existentes. No genera personas, domicilios ni cuentas. Para ampliar el catálogo en instalaciones existentes, agregá otra migración con el siguiente número disponible; no edites migraciones ya aplicadas.

Los selectores de Mi Perfil consultan únicamente `GET /api/catalogos/geografia`, que devuelve países, provincias, departamentos y localidades activas. Es de solo lectura y requiere sesión. Al cambiar un nivel se limpian sus descendientes.

`V3__cuentas_personales.sql` convierte las cuentas ADMIN existentes a USER, conserva sus datos e invalida los JWT anteriores. Esas personas deben volver a iniciar sesión. La API también rechaza las antiguas rutas administrativas aunque una cuenta heredada todavía tenga ese rol.

`V4__datos_personales.sql` agrega DNI y fecha de nacimiento a Persona. Conserva los perfiles anteriores con esos campos vacíos hasta que se completen. Al guardar Mi Perfil, ambos son obligatorios: el DNI admite 7 u 8 dígitos sin puntos y la fecha debe ser válida y no futura. El DNI se guarda como texto y la fecha como `DATE`, sin hora.

## Demostración local sin Docker

Requisitos: Java 21 y Node 22.12+. El Maven Wrapper incluido descarga Maven si hace falta. En dos terminales PowerShell:

```powershell
.\scripts\run-demo-backend.ps1
```

```powershell
.\scripts\run-frontend.ps1
```

Abrí **http://localhost:5173**. El primer script muestra las credenciales de un **usuario normal** generadas para esa ejecución. Usa H2 temporal y el mismo script geográfico; los datos se pierden al detenerlo. Se pueden indicar `-UserEmail` y `-UserPassword`. Detené los procesos con Ctrl+C.

Para crear opcionalmente un usuario de prueba en el perfil `dev`, usá `DEMO_USER_EMAIL` y `DEMO_USER_PASSWORD`. Las antiguas variables `DEMO_ADMIN_*` ya no se utilizan.

## Google OAuth2 / OIDC: configuración necesaria

La integración existente con Google sigue usando Spring Security y OpenID Connect. Retirar los ABM no requiere cambiar el cliente OAuth ni sus scopes. Después del acceso, la aplicación abre **Mi Perfil**.

Para habilitar el botón Google es necesario configurar un cliente propio en Google Cloud. No se pueden generar estas credenciales desde el código de la aplicación:

1. En Google Cloud / Google Auth Platform elegí o creá el proyecto y configurá el nombre y los datos de la aplicación en la pantalla de consentimiento.
2. Creá un cliente OAuth de tipo **Aplicación web**.
3. Registrá la **URI de redirección autorizada exacta** para el entorno que uses:

| Entorno | FRONTEND_URL | URI de redirección autorizada en Google |
| --- | --- | --- |
| Vite local | `http://localhost:5173` | `http://localhost:5173/login/oauth2/code/google` |
| Docker local | `http://localhost` | `http://localhost/login/oauth2/code/google` |
| Backend directo local | `http://localhost:5173` | `http://localhost:8080/login/oauth2/code/google` |
| Producción | `https://tu-dominio` | `https://tu-dominio/login/oauth2/code/google` |

4. Si tu aplicación tiene audiencia externa y está en modo de prueba, agregá las cuentas que vayan a probarla como usuarios de prueba.
5. Configurá **en el backend** `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` y `FRONTEND_URL`. No pongas el secreto en el frontend.

En Docker, completá `.env` y aplicá los cambios:

```sh
docker compose up -d
```

El backend carga automáticamente `.env` al iniciarse desde la raíz del proyecto o desde `backend/`. Completá allí las credenciales y reiniciá el backend. Las variables de entorno y las opciones de la JVM tienen prioridad sobre el archivo. Si configuraste valores vacíos de Google en el IDE, quitá esas variables para permitir que se utilicen las del archivo.

También podés definirlas directamente en la configuración de ejecución del IDE o en la terminal. Por ejemplo, antes de iniciar el script de demostración:

```powershell
$env:GOOGLE_CLIENT_ID='TU_CLIENT_ID.apps.googleusercontent.com'
$env:GOOGLE_CLIENT_SECRET='TU_CLIENT_SECRET'
.\scripts\run-demo-backend.ps1
```

El script usa `FRONTEND_URL=http://localhost:5173` y obtiene las credenciales Google del `.env` sin necesidad de exportarlas manualmente. Vite y Caddy ya redirigen `/oauth2` y `/login/oauth2` al backend. Para el flujo habitual local, abrí la aplicación con **localhost** y conservá ese mismo host al registrar las URIs. Si elegís `127.0.0.1`, ajustá tanto FRONTEND_URL como la URI autorizada.

El callback del protocolo es `/login/oauth2/code/google`; **no registres `/mi-perfil` como callback**. Esa es solamente la pantalla que se abre después del acceso. Los únicos scopes solicitados son `openid`, `profile` y `email`.

Si aparece `redirect_uri_mismatch`, compará esquema, host, puerto y ruta del parámetro `redirect_uri` con la URI registrada en Google. Si el botón no aparece, revisá que el backend tenga ambas credenciales y reinicialo. Si ya tenías una cuenta local con el mismo email, ingresá mediante contraseña: no se vinculan automáticamente cuentas de distinto proveedor.

Para diagnosticar el botón, abrí `http://localhost:8080/api/auth/config`: debe devolver `{"google":true}`. El frontend oculta el botón si devuelve `false` o no puede consultar ese endpoint. Después de reiniciar el backend, recargá la pantalla de inicio de sesión.

La carga opcional del archivo usa [la importación de configuración de Spring Boot](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html).

Las cuentas exclusivamente Google no tienen contraseña local y muestran esa condición en Cambiar Contraseña. No se modifica la contraseña de Google. La identidad Google exige email verificado y se reconoce por su `sub`.

Referencia oficial: [OpenID Connect de Google](https://developers.google.com/identity/openid-connect/openid-connect) y [OAuth para aplicaciones web](https://developers.google.com/identity/protocols/oauth2/web-server).

## Ejecución nativa con PostgreSQL

Creá la base `abm` y configurá las variables del backend:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/abm'
$env:DB_USER='abm'
$env:DB_PASSWORD='TU_PASSWORD_DE_BASE'
$env:JWT_SECRET='TU_SECRETO_ALEATORIO_DE_AL_MENOS_32_BYTES'
$env:FRONTEND_URL='http://localhost:5173'
cd backend
.\mvnw.cmd spring-boot:run
```

En otra terminal:

```sh
cd frontend
npm ci
npm run dev
```

## API y verificación

| Función | Endpoint |
| --- | --- |
| Registro y login | POST `/api/auth/register`, POST `/api/auth/login` |
| Cuenta actual | GET `/api/auth/me`, GET `/api/usuarios/me` |
| Perfil propio | PUT `/api/usuarios/me` |
| Contraseña propia | PUT `/api/usuarios/me/password` |
| Selectores geográficos | GET `/api/catalogos/geografia` |
| Disponibilidad de Google | GET `/api/auth/config` |

La identidad del perfil proviene del JWT; no se acepta un id de otro usuario ni cambios de rol. El DTO de dirección recibe `localidadId`; el backend deriva y verifica sus antecesores. El cambio de contraseña valida BCrypt, confirmación, diferencia y política de letras/números, e invalida los tokens anteriores. Los datos de personas o direcciones compartidas se copian antes de modificarse desde otro perfil.

```powershell
cd backend
.\mvnw.cmd verify
```

```sh
cd frontend
npm ci
npm run build
```

En Linux/macOS usá `./mvnw verify`. Las pruebas cubren la precarga idempotente, catálogo de solo lectura, ausencia de acceso a los ABM, aislamiento del perfil, contraseña, JWT y configuración OAuth. Los resultados de esta versión están en [VERIFICACION.md](VERIFICACION.md).


## DonWeb VPS con Docker y HTTPS

1. Usá un VPS Linux con Docker Engine y Compose v2 instalados. Copiá este proyecto al VPS mediante Git o SCP. No subas `.env` al repositorio.
2. Apuntá un registro DNS A del dominio al IPv4 del VPS. Si configurás AAAA, debe apuntar a su IPv6 real. Permití 80/TCP, 443/TCP y, opcionalmente, 443/UDP en el firewall del VPS y en DonWeb. Conservá el acceso SSH.
3. Desde el directorio del proyecto en el VPS:

```sh
cp .env.example .env
chmod 600 .env
openssl rand -hex 32
openssl rand -hex 32
```

4. Editá `.env`: usá cada valor para un secreto diferente; configurá `SITE_ADDRESS=tu-dominio`, `FRONTEND_URL=https://tu-dominio`, `SPRING_PROFILES_ACTIVE=prod` y, si corresponde, las credenciales Google. Registrá la URI HTTPS exacta indicada arriba.
5. Ejecutá y verificá:

```sh
docker compose up -d --build
docker compose ps
docker compose logs --tail=100 backend frontend
curl -fsS https://tu-dominio/actuator/health
curl -I https://tu-dominio
```

Caddy obtiene y renueva certificados automáticamente cuando el dominio resuelve al servidor y los puertos están accesibles. Los volúmenes `postgres_data` y `caddy_data` conservan datos y certificados. Cada persona crea su cuenta desde el registro o inicia sesión con Google. No hay despliegue realizado desde esta sesión: falta acceso al VPS, dominio y credenciales reales.

Respaldo desde Linux:

```sh
docker compose exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > backup.sql
```

Para actualizar: `git pull` y `docker compose up -d --build`. Para detener sin borrar datos: `docker compose down`. La base solo es accesible dentro de la red Docker.
