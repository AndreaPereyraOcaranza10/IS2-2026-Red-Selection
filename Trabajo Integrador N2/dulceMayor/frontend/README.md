# Dulce Mayor · Frontend (React + Vite)

Monorepo con un front **por rol**, separados entre sí y con una librería compartida de diseño, autenticación y API.

| Front | Carpeta | Rol del backend | Puerto dev | Estado |
|---|---|---|---|---|
| Preventista (mobile-first) | `preventista/` | `PREVENTOR` | 5173 | ✅ completo |
| Transportista / reparto | `transportista/` | `ALMACENERO` (ver nota) | 5174 | ✅ completo |
| Sitio público + portal del cliente | `cliente/` | `CLIENTE` | 5175 | ⏳ por construir |
| Dashboard administrativo | `admin/` | `ADMIN`, `JEFE_ADMINISTRATIVO` | 5176 | ⏳ por construir |

> **Nota de roles:** la consigna no define el rol "transportista"; asigna el reparto al equipo de despacho/logística. Hoy se mapea a `ALMACENERO` en `transportista/src/config.js`. Si el equipo crea un rol propio, se cambia esa línea y el enum del backend.
>
> Los fronts `cliente/` y `admin/` se agregan como carpetas hermanas reutilizando `shared/` (se registran en `workspaces` del `package.json` raíz y en el `Dockerfile` con `--build-arg APP=...`).

## 1. Estructura de carpetas

```
frontend/
├── package.json              # workspaces: shared, preventista, transportista
├── Dockerfile                # multi-stage; una imagen por app (ARG APP)
├── nginx.conf                # estáticos + fallback SPA
├── docker-compose.frontend.yml
├── .env.development          # VITE_USE_MOCK=true (demo sin backend)
├── tests/smoke.test.jsx      # flujos de ambos roles (vitest + Testing Library)
│
├── shared/src/               # @dulcemayor/shared  (lo usan TODOS los fronts)
│   ├── styles/app.css        # tokens (variables CSS) + estilos de componentes
│   ├── components/           # Layout, BottomNav, Fab, Sheet, Dialog, Toast, MapView,
│   │                         # SwipeRow, StopCard, Timeline, QtyControl, Field, States, Chip, Icon
│   │                         # (MapView = mapa real Leaflet + OpenStreetMap)
│   ├── auth/                 # AuthContext (JWT), RequireAuth, LoginPage, ChangePasswordPage
│   ├── api/                  # http.js (cliente REST + Adapter mock/real), ApiError.js
│   ├── mock/router.js        # API simulada en memoria, mismos contratos que el backend
│   ├── hooks/hooks.js        # useAsync, useRefresh, usePullToRefresh
│   └── utils/                # format, password, domain (roles, estados, medios de pago)
│
├── preventista/src/
│   ├── App.jsx               # rutas + providers
│   ├── api.js                # Facade de la API del preventista
│   ├── config.js             # roles permitidos, categorías
│   ├── context/CartContext.jsx
│   ├── components/           # PreventistaNav, ProductCard, StopSheet
│   └── pages/                # RoutePage, ZonePage, CatalogPage, OrderPage, OrderDonePage,
│                             # OrdersPage, OrderDetailPage, PointOfSaleFormPage
│
└── transportista/src/
    ├── App.jsx
    ├── api.js                # Facade de la API de reparto
    ├── config.js
    ├── components/           # DeliveryNav, PaymentNotice
    └── pages/                # DeliveryRoutePage, DeliveryMapPage, DeliveriesDonePage,
                              # NextDeliveryPage, DeliveryRegisteredPage, DeliveryDetailPage
```

**Separación estricta entre fronts:** `preventista` y `transportista` no se importan entre sí; solo dependen de `shared`. Por eso el transportista *no puede* tomar pedidos ni llamar al cliente: esas pantallas y endpoints ni siquiera existen en su código.

## 2. Patrones aplicados y dónde están

> El **Template Method obligatorio** de la consigna vive en el backend (`BaseController<T, ID>` y `BaseService<T, ID>`). Este front consume ese contrato REST uniforme. En el front se aplican estos patrones:

| Patrón | Dónde | Para qué |
|---|---|---|
| **Template Method (de UI)** | `shared/src/components/Layout.jsx` → `Layout` | Esqueleto fijo de pantalla (barra superior, contenido scrolleable, barra de acción, navegación inferior, FAB, pull-to-refresh). Cada página completa solo los "slots" (`title`, `actions`, `bar`, `nav`, `fab`, `children`). |
| **Adapter** | `shared/src/api/http.js` | Un único `api.get/post/...`; por debajo habla con `fetch` (backend real) o con `mock/router.js` según `VITE_USE_MOCK`. Ninguna pantalla cambia al pasar de demo a producción. |
| **Facade** | `preventista/src/api.js`, `transportista/src/api.js` | Las pantallas llaman `preventistaApi.createOrder(...)`, no conocen URLs ni verbos HTTP. |
| **Strategy (optimización de ruta)** | `shared/src/utils/routing.js` → `optimizeStops` (usada vía `RouteOptimizer.jsx` por preventista y transportista) | Dos estrategias para ordenar las paradas pendientes: OSRM `/trip` (orden óptimo real por calles, origen fijo) y, si el servicio no responde, vecino más cercano + 2-opt con distancias estimadas. Devuelve `source: 'osrm' \| 'estimada'` para que la UI lo informe. El backend podrá reemplazarla guardando solo el orden (`PUT /route/order`). |
| **Strategy (consumo)** | `transportista/src/components/PaymentNotice.jsx` | Muestra cómo cobrar según `paymentMethod` (EFECTIVO / TRANSFERENCIA / BILLETERA_QR). El texto (alias, QR) lo arma la estrategia de pago del backend: no hay alias hardcodeado en el front. |
| **Observer (Context)** | `AuthContext.jsx`, `CartContext.jsx`, `Toast.jsx` | Estado compartido con suscripción de componentes (sesión, carrito, snackbars). |
| **Guard / Proxy de rutas** | `AuthContext.jsx` → `RequireAuth` | Sin sesión → login; con `mustChangePassword` → cambio de clave; rol no permitido → login. |
| **DTO** | `shared/src/mock/router.js` (mappers) y todas las pantallas | El front solo conoce DTOs (`OrderDTO`, `StopDTO`, ...), nunca entidades. |

## 3. Contratos de la API REST (lo que el backend debe exponer)

Todas las rutas (salvo login) exigen `Authorization: Bearer <JWT>`. Errores: `{ "status": 400, "message": "...", "code": "...", "errors": [{ "field": "cuit", "message": "..." }] }` (lo genera el `@RestControllerAdvice`).

**Seguridad / regla `dulcemayor`**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `POST /api/auth/login` | `{ username, password }` | `{ token, user: { id, username, fullName, role, mustChangePassword } }` | 200 · 401 |
| `POST /api/auth/first-login/change-password` | `{ newPassword }` (token restringido) | igual que login, con `mustChangePassword:false` | 200 · 400 (no puede ser `dulcemayor`) |
| *(cualquier otra ruta con `mustChangePassword=true`)* | — | `{ code: "PASSWORD_CHANGE_REQUIRED" }` | **403** |

**Preventista (`PREVENTOR`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `GET /api/preventor/route/today` | — | `{ date, kmRemaining, stops: StopDTO[] }` | 200 |
| `POST /api/preventor/route/stops/{id}/visit` | — | — | 204 |
| `PUT /api/preventor/route/order` | `{ stopIds: number[] }` (todas las paradas pendientes, en el orden a visitar) | `{ stops: StopDTO[] }` (visitadas primero, renumeradas) | 200 · 400 si faltan o sobran paradas |
| `GET /api/preventor/zone` | — | `StopDTO[]` + `inRoute` | 200 |
| `GET /api/products` | — | `ProductDTO[]` (**solo con stock**) | 200 |
| `POST /api/promotions/validate` | `{ code }` | `{ valid, discountPercent }` | 200 |
| `POST /api/orders` | `{ pointOfSaleId, promoCode?, items:[{productId,quantity}] }` | `{ order: OrderDTO, nextStop: StopDTO \| null }` | 201 · 400 |
| `GET /api/orders` · `GET /api/orders/{id}` | — | `OrderDTO[]` · `OrderDTO` | 200 · 404 |
| `POST /api/points-of-sale` | `{ name, cuit, contactName, email, phone, address, lat?, lng? }` | `{ id, name }` | 201 · 400 |
| `GET /api/geo/reverse?lat=&lng=` | — | `{ address, lat, lng }` | 200 |

**Transportista (`ALMACENERO`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `GET /api/delivery/route/today` | — | `{ date, kmRemaining, stops: DeliveryStopDTO[] }` | 200 |
| `PUT /api/delivery/route/order` | `{ stopIds: number[] }` (todas las entregas pendientes, en el orden a recorrer) | `{ stops: StopDTO[] }` (entregadas primero, renumeradas) | 200 · 400 si faltan o sobran entregas |
| `GET /api/delivery/stops/{id}` | — | `{ stop, state, items[], paymentInstruction, trace }` | 200 · 404 |
| `POST /api/delivery/stops/{id}/deliver` | — | `{ delivered, nextStop \| null, emailSent }` (el backend envía el mail al próximo comercio) | 200 · 404 |

**DTOs principales**

```
StopDTO          { id, sequence, name, address, distanceKm, lat, lng, contactName, phone, status: PENDIENTE|VISITADO }
DeliveryStopDTO  { id, sequence, name, address, distanceKm, lat, lng, contactName, status: PENDIENTE|ENTREGADO,
                   deliveredAt, paymentMethod: EFECTIVO|TRANSFERENCIA|BILLETERA_QR, total }
ProductDTO       { id, name, presentation, price, category, stockLevel: OK|LOW|NONE }
OrderDTO         { id, state: CARGADO|PENDIENTE_STOCK|FACTURADO|EN_REPARTO|ENTREGADO|ANULADO, total, discount, createdAt,
                   pointOfSale:{id,name,address,lat,lng}, items:[{productId,name,presentation,unitPrice,quantity}],
                   trace:{ events:[{state,at}], carrier:{name,vehicle} | null } }
```

## 4. Ejecución y despliegue

**Desarrollo (sin backend, API simulada):**

```bash
npm install
npm run dev:preventista      # http://localhost:5173  usuario: preventista.mza
npm run dev:transportista    # http://localhost:5174  usuario: despacho.mza
npm test                     # 7 tests de flujos
```
Clave inicial de ambos usuarios demo: `dulcemayor` (la app obliga a cambiarla). Tras cambiarla, recargar el servidor de dev reinicia los datos de ejemplo. Todas las pantallas de vacío, carga y error se disparan con condiciones reales (ruta vacía, request en curso, API caída).

**Con el backend real:** poner `VITE_USE_MOCK=false` en `.env.development`; Vite redirige `/api` a `VITE_PROXY_TARGET` (por defecto `http://localhost:8080`).

**Variables de entorno (se fijan al *build*)**

| Variable | Default | Uso |
|---|---|---|
| `VITE_USE_MOCK` | `false` en Docker, `true` en dev | `true` = API simulada |
| `VITE_API_URL` | vacío (mismo origen) | Base de la API si no va detrás del proxy |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | Solo dev: destino del proxy de Vite |
| `VITE_TILE_URL` | `https://tile.openstreetmap.org/{z}/{x}/{y}.png` | Servidor de teselas del mapa (ver nota del mapa) |
| `VITE_ROUTING_URL` | `https://router.project-osrm.org` | Servicio de ruteo OSRM para "Cómo llegar" (el público es solo demo) |

**Docker (una imagen por front):**

```bash
docker build --build-arg APP=preventista   -t dulcemayor-preventista .
docker build --build-arg APP=transportista -t dulcemayor-transportista .
docker compose -f docker-compose.frontend.yml up --build   # prueba local con mock: :8081 y :8082
```

**Integración al `docker-compose.yml` productivo (DonWeb VPS)** — servicios a sumar junto a PostgreSQL y backend:

```yaml
  preventista:
    build: { context: ./frontend, args: { APP: preventista } }
    restart: unless-stopped
  transportista:
    build: { context: ./frontend, args: { APP: transportista } }
    restart: unless-stopped
```

Caddyfile del reverse proxy (un subdominio por front, HTTPS automático):

```
preventista.dulcemayor.com.ar   { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy preventista:80 } }
reparto.dulcemayor.com.ar       { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy transportista:80 } }
```

## Decisiones a tener en cuenta

- **JWT en `sessionStorage`**: simple y sin CSRF, pero expuesto a XSS. Para producción endurecida, el backend puede emitir una cookie `HttpOnly; Secure; SameSite=Strict` y se elimina el manejo del token en `http.js`.
- **Mapa**: `MapView.jsx` usa Leaflet con teselas de OpenStreetMap, dentro de la app (pines numerados, ruta punteada, atribución obligatoria de OSM). El servidor público de OSM es para uso liviano: para producción con tráfico real conviene un proveedor de teselas (MapTiler, Stadia, etc.) y cambiar solo `VITE_TILE_URL`. "Cómo llegar" se resuelve dentro de la app (`Directions.jsx`): pide la ubicación del dispositivo, consulta un servicio de ruteo compatible con OSRM (`utils/routing.js`, variable `VITE_ROUTING_URL`) y dibuja la ruta por calles sobre el mapa con tiempo y distancia. "Optimizar mi ruta" (preventista, `RoutePage.jsx`) ordena todas las paradas pendientes por el camino más corto desde la ubicación actual, guarda el orden en el backend y dibuja el recorrido completo; al confirmar un pedido, `OrderDonePage.jsx` recalcula desde el comercio recién visitado y propone el siguiente punto más eficiente con el recorrido restante; el transportista tiene "Optimizar mi reparto" (`DeliveryRoutePage.jsx`), pero su orden no cambia solo después de cada entrega, porque el aviso por correo al próximo comercio depende de ese orden; "Abrir en Google Maps" queda como opción secundaria. El servidor público de OSRM es solo para la demo: en producción apuntar `VITE_ROUTING_URL` a una instancia propia o a un proveedor. Con `VITE_USE_MOCK=true` también se pide la ruta real por calles (hace falta internet); si el servicio no responde, o `VITE_ROUTING_URL=off`, se dibuja una línea recta de respaldo. Sin permiso de ubicación se usa un origen de demo. Las coordenadas de la demo son ilustrativas; las reales salen de `lat/lng` que guarda el alta de punto de venta.
- **Llamar al cliente**: solo existe en el preventista (`tel:` en el swipe de la tarjeta). El transportista no tiene ni el botón ni los teléfonos.