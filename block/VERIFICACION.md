# Verificación · versión de cuenta personal

Verificada el 30 de octubre de 2026.

| Comprobación | Resultado |
| --- | --- |
| Backend compilado y empaquetado con Maven, release 21 | Correcto |
| `ApiIntegrationTest` | 15 pruebas correctas |
| `OAuthConfigurationTest` | 3 pruebas correctas |
| Total de pruebas | 18, sin fallos, errores ni omisiones |
| TypeScript y compilación de producción Vite | Correctos |
| PostgreSQL, backend y Caddy en Compose aislado | Correctos |
| Flyway V1, V2 y V3 e Hibernate validate | Tres migraciones aplicadas y esquema válido |
| API a través de Caddy | 28 solicitudes con estados HTTP esperados |
| Repetición del SQL geográfico en PostgreSQL | Sin duplicados: 1 país, 4 provincias, 4 departamentos y 4 localidades |
| Conversión de cuenta heredada ADMIN a USER | Perfil conservado y versión de credenciales incrementada |
| Migración V4 de datos personales sobre PostgreSQL | Perfil anterior conservado con campos vacíos; DNI con cero inicial y fecha bisiesta guardados correctamente |
| OpenAPI | Catálogo y cuenta personal presentes; endpoints ABM y dashboard ausentes |
| Carga automática del `.env` local | Instancia temporal sin variables Google exportadas: `/api/auth/config` devuelve `google=true` y el inicio OAuth redirige a Google con HTTP 302 |

## Cobertura

- Registro crea USER y rechaza la asignación de rol desde el cliente.
- Catálogo autenticado de solo lectura, sin personas ni direcciones privadas.
- Antiguas rutas administrativas bloqueadas para GET, POST, PUT y DELETE, incluso con una cuenta heredada ADMIN.
- Creación, modificación y recarga del perfil propio; no duplica Persona ni Dirección en actualizaciones sucesivas.
- Rechazo de ids de cuenta enviados por el cliente y de email ya ocupado; aislamiento de otras cuentas.
- Copia de datos compartidos antes de modificar el perfil.
- Rechazo de localidades inexistentes, eliminadas o con antecesores inactivos; el catálogo también oculta esos descendientes.
- Cambio de contraseña: contraseña actual, confirmación, diferencia, rechazo de la anterior y revocación de todos los JWT anteriores.
- Cuenta exclusivamente Google sin cambio de contraseña local ni modificación del email verificado desde el perfil.
- Estrategia Google crea un usuario normal y evita vincular automáticamente una cuenta local.
- Precarga SQL repetible e independiente de ids fijos; no crea datos personales.
- Errores de autenticación en UTF-8.
- DNI y fecha de nacimiento: creación, actualización y recarga, ceros iniciales del DNI, fecha bisiesta válida y rechazo de campos vacíos, formatos inválidos y fechas futuras.
- OAuth solicita state/OIDC, rechaza un callback falsificado y construye el callback correcto detrás de un proxy HTTPS.

## Límites y ejecución existente

Las pruebas automatizadas usan H2; adicionalmente se verificaron las migraciones y los flujos de cuenta sobre PostgreSQL real, dentro de contenedores aislados en el puerto 8088. No se detuvieron los procesos que el usuario ya tenía abiertos en 8080 y 5173. Para que esos procesos usen la versión actual, debe reiniciarse el backend y recargarse el frontend.

La versión base de cuenta personal se construyó en Docker y ejecutó 16 pruebas con Java 21. La actualización de DNI y fecha de nacimiento compiló y ejecutó 18 pruebas en el host con JDK 26 y release 21. V4 se comprobó además sobre un PostgreSQL aislado, aplicándola a una Persona creada antes de agregar las columnas. Los reportes locales se generan en `backend/target/surefire-reports/`.

No hay un navegador conectado a las herramientas de esta sesión: no se realizó revisión visual ni interacción de formularios en navegador. Sí se compilaron los formularios y se verificaron sus operaciones por HTTP.

Se verificaron la detección de las credenciales Google y la redirección inicial; no se completaron el consentimiento ni el intercambio real con una cuenta Google. README detalla las variables y URIs autorizadas. No se realizó despliegue en DonWeb.

El entorno Docker temporal de verificación se elimina al terminar; la base y los servicios existentes del usuario se conservan.
