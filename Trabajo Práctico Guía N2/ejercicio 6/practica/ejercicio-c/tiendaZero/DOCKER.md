# Dockerización de tiendaZero

## Requisitos
- Docker Engine
- Docker Compose v2 (`docker compose`)
- Un archivo `.env` local con contraseñas y credenciales reales.

## Primera ejecución
1. Revisá el archivo `.env` y reemplazá `MYSQL_ROOT_PASSWORD` por una contraseña segura.
2. Desde la carpeta raíz (donde están `Dockerfile` y `docker-compose.yml`), ejecutá:
   ```bash
   docker compose config
   docker compose up --build -d
   ```
3. Revisá los contenedores y los registros:
   ```bash
   docker compose ps
   docker compose logs -f tienda_zero_app
   ```
4. Abrí `http://localhost:8080` (o el puerto que hayas configurado en `APP_PORT`).

## Detener y volver a iniciar
```bash
docker compose down
docker compose up -d
```
Los datos de MySQL quedan en el volumen `tienda_zero_data`. `docker compose down -v`
elimina también ese volumen y, por lo tanto, borra la base de datos persistida.

## Cambios de configuración
`application.properties` permite configurar la conexión mediante `DB_URL`, `DB_USERNAME`
y `DB_PASSWORD`. Compose usa el nombre del servicio `tienda_zero_db` como host de MySQL;
dentro de un contenedor, `localhost` apunta al propio contenedor, no a otro servicio.

Las credenciales de correo y Mercado Pago se leen desde variables de entorno. Para probar
webhooks de Mercado Pago, la URL configurada debe ser pública y accesible por HTTPS; una
URL `localhost` no sirve para recibir notificaciones externas.

## Importante antes de producción
- Cambiá todas las contraseñas y tokens que hayan sido compartidos o guardados en el `.env`
  anterior; el archivo `.env` no debe subirse al repositorio.
- Configurá `MERCADOPAGO_BASE_URL`, `MERCADOPAGO_WEBHOOK_URL`, `APP_ACTIVACION_URL` y
  `APP_TIENDA_URL` con el dominio HTTPS de producción.
- La configuración actual de Hibernate usa `ddl-auto=update`, apropiada para desarrollo,
  pero para producción conviene gestionar el esquema con migraciones y copias de seguridad.
- Antes de exponer el servidor a Internet, revisar autenticación, HTTPS, firewall y secretos.
