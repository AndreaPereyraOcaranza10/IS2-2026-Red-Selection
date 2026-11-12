# Backend

## BK-12: newsletter mensual de ofertas

El backend envía el correo automáticamente el día 5 de cada mes a las 09:00 (zona `America/Argentina/Mendoza`). Incluye productos activos marcados en oferta que tengan precio vigente y stock positivo. Cada responsable de comercios activos recibe un solo mensaje por correo principal; los errores individuales se cuentan y no detienen los demás envíos.

Para la demo, iniciá sesión como `JEFE_ADMINISTRATIVO` y llamá `POST http://localhost:8080/api/newsletter/enviar` con el token JWT. La respuesta informa ofertas incluidas, destinatarios, envíos exitosos, fallidos y clientes sin correo. El endpoint también puede servir para probar antes del día programado.

Para programar un solo envío 10 minutos después, llamá `POST http://localhost:8080/api/newsletter/programar` con `{"minutos":10}` y el token de Jefe. El backend responde con la hora programada. La tarea se ejecuta mientras el backend permanezca levantado; si se reinicia, hay que volver a programarla.

La plantilla se puede consultar y editar en `/api/configuracion-correo/NEWSLETTER`. Marcadores disponibles: `{{responsable}}`, `{{fecha}}` y `{{productos}}`. El correo usa la configuración SMTP de `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER` y `MAIL_PASSWORD`.

Para cambiar el horario se pueden definir `NEWSLETTER_CRON` (formato cron de seis campos de Spring) y `NEWSLETTER_ZONE`.

## Actualización del estado de facturas

Las bases MySQL/MariaDB creadas antes de esta versión pueden tener `factura_cliente.estado` como un `ENUM` que no admite `PAGADA`. Ejecutá una sola vez el script `src/main/resources/db/manual/V20261111__factura_estado_varchar.sql` sobre la base `dulce_mayor` y reiniciá el backend. El script conserva los estados existentes y cambia la columna a `VARCHAR(20)`.

## Actualización de la relación entre pagos y pedidos

Si la base existente todavía no tiene las columnas agregadas al flujo de pagos, ejecutá una sola vez los scripts `src/main/resources/db/manual/V20261112__pago_pedido_id.sql` y `src/main/resources/db/manual/V20261112__pago_referencia_externa.sql` sobre `dulce_mayor`, y reiniciá el backend. `pedido_id` admite `NULL` para mantener los pagos históricos asociados únicamente a facturas; `referencia_externa` guarda el ID de la orden de Mercado Pago. `factura_id` también debe admitir `NULL`, porque el pago del pedido se crea antes que la factura: si el esquema existente la marca `NOT NULL`, ejecutá `src/main/resources/db/manual/V20261112__pago_factura_nullable.sql` una vez. Si aparecen errores `Data truncated` al registrar estados, ejecutá una sola vez `src/main/resources/db/manual/V20261112__historial_estado_varchar.sql` y `src/main/resources/db/manual/V20261112__pedido_estados_varchar.sql` para cambiar las columnas de estado del historial y del pedido a `VARCHAR`.

## IN-04 / BK-14: pago con QR de Mercado Pago

El proveedor real usa la API Orders de Mercado Pago para generar un QR dinámico por orden. El Access Token se configura solo en el backend; no debe ponerse en el frontend ni subirse al repositorio. La caja QR debe existir previamente en Mercado Pago y su identificador externo se configura como `MERCADOPAGO_POS_ID`.

En `backend/.env` (o en las variables de entorno del servidor), configurá:

```properties
PAGOS_PASARELA=mercadopago
MERCADOPAGO_ACCESS_TOKEN=APP_USR-... # Usá primero las credenciales de prueba
MERCADOPAGO_POS_ID=ID_EXTERNO_DE_LA_CAJA
MERCADOPAGO_WEBHOOK_SECRET=secreto_de_webhooks
MERCADOPAGO_QR_EXPIRATION=PT15M # opcional; QR válido por 15 minutos
```

En Mercado Pago Developers, configurá el webhook de Orders (`Order (Mercado Pago)`) para la URL pública HTTPS `https://TU_DOMINIO/api/webhooks/mercadopago/orders` y copiá su clave secreta a `MERCADOPAGO_WEBHOOK_SECRET`. El endpoint valida `x-signature`; luego el backend consulta `GET /v1/orders/{id}` con el Access Token antes de acreditar el pago. Para probar desde desarrollo local, Mercado Pago necesita una URL HTTPS pública que reenvíe las notificaciones al backend.

La creación del QR requiere la cuenta vendedora y la sucursal/caja configuradas en Mercado Pago. Para pruebas, usá credenciales y usuarios de prueba de comprador/vendedor. Si Mercado Pago no está configurado, usá el modo demo con `PAGOS_PASARELA=simulada` (también es el valor predeterminado).

## BK-13: asistente para armar presupuestos

El endpoint `POST /api/chatbot/presupuestos` recibe una consulta en español y devuelve una propuesta de productos y cantidades. Requiere Ollama disponible en el equipo donde corre el backend. El proveedor predeterminado es `llama3.1:8b`.

### Instalar Ollama en Windows

1. Descargá e instalá Ollama desde [ollama.com/download/windows](https://ollama.com/download/windows).
2. Cuando termine la instalación, cerrá y volvé a abrir PowerShell para que se actualice el `PATH`.
3. Comprobá que el comando esté disponible y descargá el modelo:

```powershell
ollama --version
ollama pull llama3.1:8b
```

La descarga puede ocupar varios GB. Ollama guarda el modelo en el directorio de datos del usuario de Windows (normalmente `%USERPROFILE%\.ollama\models`), fuera de la carpeta del proyecto; no se agrega al repositorio.

La aplicación de Ollama para Windows normalmente inicia el servicio local automáticamente. No ejecutes `ollama serve` si ya está activo; usalo solo si necesitás iniciar el servicio manualmente y no hay otro proceso escuchando en el puerto `11434`. El backend se conecta por defecto a `http://127.0.0.1:11434`.

Si Ollama usa otra dirección o modelo, configurá estas variables antes de iniciar Spring:

```properties
CHATBOT_OLLAMA_URL=http://localhost:11434
CHATBOT_OLLAMA_MODELO=llama3.1:8b
```

Ejemplo de llamada:

```powershell
curl.exe -X POST http://localhost:8080/api/chatbot/presupuestos `
  -H "Content-Type: application/json" `
  -d '{"mensaje":"Necesito 5 bultos de gomitas y 2 de chicles"}'
```

El backend comprueba que los productos existan, tengan precio vigente y cuenten con stock suficiente. Calcula los subtotales y el total con los precios del catálogo. Si falta stock, el total queda `null` y la respuesta incluye observaciones. El asistente solo propone IDs y cantidades: no puede definir precios. La consulta no crea el pedido ni reserva stock; al confirmar el pedido, el flujo habitual vuelve a validar el inventario.
