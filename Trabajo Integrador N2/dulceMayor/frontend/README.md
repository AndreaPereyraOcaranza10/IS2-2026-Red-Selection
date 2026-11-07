# Dulce Mayor · Frontend (React + Vite)

Monorepo con un front **por rol**, separados entre sí y con una librería compartida de diseño, autenticación y API.

| Front | Carpeta | Rol del backend | Puerto dev | Estado |
|---|---|---|---|---|
| Preventista (mobile-first) | `preventista/` | `PREVENTOR` | 5173 | ✅ completo |
| Transportista / reparto | `transportista/` | `ALMACENERO` (ver nota) | 5174 | ✅ completo |
| Sitio público + portal del cliente | `cliente/` | `CLIENTE` | 5175 | ✅ completo |
| Centro de mando administrativo | `admin/` | `ADMIN`, `JEFE_ADMINISTRATIVO`, `ALMACENERO` | 5176 | ✅ completo |

> **Nota de roles:** la consigna no define el rol "transportista"; asigna el reparto al equipo de despacho/logística. Hoy se mapea a `ALMACENERO` en `transportista/src/config.js`. Si el equipo crea un rol propio, se cambia esa línea y el enum del backend.
>
> `cliente/` y `admin/` nacieron de un front en TypeScript de una sola app, que se migró a este formato: JSX, tokens de diseño compartidos (`shared/src/styles/tokens.css`: paleta, fuente Nunito, radios), sesión JWT compartida y los mismos estados de pedido que el resto. Los datos que ese front inventaba (reseñas, CBU/titular, CUIT del pie, horario de corte) se quitaron: deben salir del backend o cargarse en la sucursal.
>
> `ALMACENERO` lo usan `transportista` y `admin`: confirmar con la cátedra si el reparto necesita un rol propio.

## 1. Estructura de carpetas

```
frontend/
├── package.json              # workspaces: shared, preventista, transportista, cliente, admin
├── Dockerfile                # multi-stage; una imagen por app (ARG APP)
├── nginx.conf                # estáticos + fallback SPA
├── docker-compose.frontend.yml
├── .env.development          # VITE_USE_MOCK=true (demo sin backend)
├── tests/smoke.test.jsx      # flujos de preventista y transportista (vitest + Testing Library)
├── tests/web.test.jsx        # portal cliente y centro de mando
├── tests/routes.test.js      # adapter de rutas y contrato con el backend
│
├── shared/src/               # @dulcemayor/shared  (lo usan TODOS los fronts)
│   ├── core.js               # @dulcemayor/core: entrada de los fronts web (sin estilos móviles)
│   ├── styles/tokens.css     # paleta, fuente y radios: ÚNICA fuente de diseño de los 4 fronts
│   ├── styles/app.css        # estilos de componentes móviles (importa tokens.css)
│   ├── styles/web-base.css   # estilos web comunes de cliente y admin
│   ├── web/StaffAuth.jsx     # login y cambio de clave del personal (admin)
│   ├── components/           # Layout, BottomNav, Fab, Sheet, Dialog, Toast, MapView,
│   │                         # SwipeRow, StopCard, Timeline, QtyControl, Field, States, Chip, Icon
│   │                         # (MapView = mapa real Leaflet + OpenStreetMap)
│   ├── auth/                 # AuthContext (JWT), RequireAuth, LoginPage, ChangePasswordPage
│   ├── api/                  # http.js (cliente REST + Adapter mock/real), ApiError.js
│   ├── api/routeApi.js       # Adapter de la API de rutas del backend → modelo de las pantallas
│   ├── mock/routes.js        # simula /api/rutas y /api/ruteo (estimación en línea recta)
│   ├── mock/router.js        # API simulada (+ webRoutes.js: catálogo público, sucursales, stock, estados) en memoria, mismos contratos que el backend
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
├── transportista/src/
│   ├── App.jsx
│   ├── api.js                # Facade de la API de reparto
│   ├── config.js
│   ├── components/           # DeliveryNav, PaymentNotice
│   └── pages/                # DeliveryRoutePage, DeliveryMapPage, DeliveriesDonePage,
│                             # NextDeliveryPage, DeliveryRegisteredPage, DeliveryDetailPage
│
├── cliente/src/              # sitio público + portal del cliente minorista
│   ├── App.jsx               # /, /producto/:id, /login, /pedidos*, /perfil*   (* requiere CLIENTE)
│   ├── api.js                # Facade (clienteApi)
│   ├── config.js
│   ├── context/              # CartContext, CatalogContext
│   ├── hooks/useDollarQuote.js
│   ├── components/           # Header, Hero, CategoryBar, ProductCard, CartPanel, Branches,
│   │                         # DollarQuoteBadge, Footer, SiteLayout
│   └── pages/                # HomePage, ProductDetailPage, LoginPage, OrdersPage, ProfilePage
│
└── admin/src/                # centro de mando (escritorio)
    ├── App.jsx               # /login, /cambiar-clave, /:section? (inicio|productos|stock|pedidos|facturacion|despacho|cuenta|sucursales)
    ├── api.js                # Facade (adminApi)
    ├── config.js, utils.js   # roles permitidos, circuito de estados, formateo
    ├── components/           # OrdersTable, InventoryTable, DataRows, Kpi, ManualOrderModal, BranchesAdmin
    └── pages/DashboardPage.jsx
```

**Separación estricta entre fronts:** ningún front importa a otro; solo dependen de `shared` (los web, vía `@dulcemayor/core`). Por eso el transportista *no puede* tomar pedidos ni llamar al cliente: esas pantallas y endpoints ni siquiera existen en su código.

## 2. Patrones aplicados y dónde están

> El **Template Method obligatorio** de la consigna vive en el backend (`BaseController<T, ID>` y `BaseService<T, ID>`). Este front consume ese contrato REST uniforme. En el front se aplican estos patrones:

| Patrón | Dónde | Para qué |
|---|---|---|
| **Template Method (de UI)** | `shared/src/components/Layout.jsx` → `Layout` | Esqueleto fijo de pantalla (barra superior, contenido scrolleable, barra de acción, navegación inferior, FAB, pull-to-refresh). Cada página completa solo los "slots" (`title`, `actions`, `bar`, `nav`, `fab`, `children`). |
| **Adapter** | `shared/src/api/http.js` | Un único `api.get/post/...`; por debajo habla con `fetch` (backend real) o con `mock/router.js` según `VITE_USE_MOCK`. Ninguna pantalla cambia al pasar de demo a producción. |
| **Facade** | `preventista/src/api.js`, `transportista/src/api.js`, `cliente/src/api.js`, `admin/src/api.js` | Las pantallas llaman `preventistaApi.createOrder(...)`, no conocen URLs ni verbos HTTP. |
| **Strategy + Adapter (ruteo, en el BACKEND)** | `backend/.../patrones/strategy/OptimizadorRuta` (+ `OsrmOptimizador`, `VecinoMasCercanoOptimizador`) y `patrones/adapter/OsrmAdapter` | El orden de las paradas y el trazado por calles se calculan **solo en el backend** (OSRM, con respaldo en línea recta). El front no tiene lógica de ruteo: `shared/src/api/routeApi.js` (Adapter DTO → modelo de pantalla) y `shared/src/utils/routing.js` solo piden el resultado. El plan indica `source: 'osrm' \| 'estimada'` para que la UI lo informe. |
| **Strategy (consumo)** | `transportista/src/components/PaymentNotice.jsx` | Muestra cómo cobrar según `paymentMethod` (EFECTIVO / TRANSFERENCIA / BILLETERA_QR). El texto (alias, QR) lo arma la estrategia de pago del backend: no hay alias hardcodeado en el front. |
| **Observer (Context)** | `AuthContext.jsx`, `CartContext.jsx`, `Toast.jsx` | Estado compartido con suscripción de componentes (sesión, carrito, snackbars). |
| **Template Method (de UI, web)** | `cliente/src/components/SiteLayout.jsx` | Esqueleto del sitio (cabecera, carrito, pie); cada página aporta su contenido. |
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

**Rutas y ruteo (backend real, DTOs en español; ver `backend/README-rutas.md`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `GET /api/rutas/preventista/{id}/hoy` · `GET /api/rutas/transportista/{id}/hoy` | — | `RutaDTO` (`id, fecha, tipoRuta, estado, kmRestantes, paradas[]`) | 200 · 404 |
| `POST /api/rutas/paradas/{id}/visita` | — | `ParadaRutaDTO` | 200 · 404 · 422 |
| `POST /api/rutas/paradas/{id}/entrega` | — | `{ entregada, siguiente \| null, correoEnviado }` (envía el correo al próximo comercio) | 200 · 404 · 422 |
| `POST /api/rutas/{rutaId}/optimizar` | `{ latitud?, longitud? }` | `{ ruta, distanciaKm, duracionMin, geometria: [lat,lng][], fuente: "OSRM" \| "ESTIMADA" }` (guarda el orden) | 200 · 404 |
| `PUT /api/rutas/{rutaId}/orden` | `{ paradaIds: string[] }` (todas las pendientes) | `RutaDTO` | 200 · 422 |
| `GET /api/ruteo/trayecto?desdeLat=&desdeLng=&hastaLat=&hastaLng=` | — | `{ distanciaKm, duracionMin, geometria }` ("Cómo llegar") | 200 · 422 · 503 |

El empleado va en la URL por `user.employeeId` (o `user.id`) de la sesión. Errores del backend: `ProblemDetail` (`detail`, `errores`); `http.js` los traduce a `ApiError`.


**Preventista (`PREVENTOR`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
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
| `GET /api/delivery/stops/{id}` | — | `{ stop, state, items[], paymentInstruction, trace }` | 200 · 404 |

**Cliente (público y `CLIENTE`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `GET /api/products` *(público)* | — | `ProductDTO[]` con `brand`, `unitPrice` (solo con stock) | 200 |
| `GET /api/companies/branches` *(público)* | — | `BranchDTO[]` | 200 |
| `GET /api/exchange-rate/usd` *(público)* | — | `{ buy, sell, updatedAt }` (el backend adapta la API externa del dólar) | 200 · 502 |
| `GET /api/locations/provinces` · `/provinces/{id}/departments` · `/departments/{id}/localities` | — | `{ id, name }[]` | 200 |
| `POST /api/auth/register` | `{ fullName, email, password }` | sesión como login | 201 · 409 |
| OAuth2 social login (Google) | redirección del backend | sesión como login | 302 |
| `GET /api/profile` · `PUT /api/profile` | datos del perfil | `ProfileDTO` | 200 · 400 |
| `GET /api/orders` · `POST /api/orders` | como preventista; el cliente solo ve y crea los de su punto de venta | `OrderDTO` | 200 · 201 |

**Centro de mando (`ADMIN`, `JEFE_ADMINISTRATIVO`, `ALMACENERO`)**

| Método y ruta | Request | Response | Status |
|---|---|---|---|
| `GET /api/admin/products` | — | `InventoryDTO[]` (`stock`, `stockMinimum`) | 200 |
| `PATCH /api/admin/products/{id}/stock` | `{ stock }` | `InventoryDTO` | 200 · 400 |
| `GET /api/points-of-sale` | — | `{ id, name, address }[]` | 200 |
| `PATCH /api/orders/{id}/status` | `{ state }` | `OrderDTO` | 200 · 409 `INVALID_TRANSITION` / `INSUFFICIENT_STOCK` |
| `GET/POST /api/admin/companies/branches` · `PUT/DELETE /api/admin/companies/branches/{id}` | `BranchDTO` | `BranchDTO` | 200 · 201 · 204 · 404 |

Circuito de estados: `CARGADO → FACTURADO | PENDIENTE_STOCK | ANULADO`, `PENDIENTE_STOCK → FACTURADO | ANULADO`, `FACTURADO → EN_REPARTO | ANULADO`, `EN_REPARTO → ENTREGADO`. Facturar descuenta stock; si no alcanza, el backend responde 409.

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
npm run dev:cliente          # http://localhost:5175  usuario: cliente@demo.com / cliente123 (o registrarse)
npm run dev:admin            # http://localhost:5176  usuarios: admin.mza, jefe.mza (clave inicial dulcemayor)
npm test                     # 21 tests
```
Clave inicial de los usuarios del personal: `dulcemayor` (la app obliga a cambiarla). Tras cambiarla, recargar el servidor de dev reinicia los datos de ejemplo. Todas las pantallas de vacío, carga y error se disparan con condiciones reales (ruta vacía, request en curso, API caída).

**Con el backend real:** poner `VITE_USE_MOCK=false` en `.env.development`; Vite redirige `/api` a `VITE_PROXY_TARGET` (por defecto `http://localhost:8080`).

**Variables de entorno (se fijan al *build*)**

| Variable | Default | Uso |
|---|---|---|
| `VITE_USE_MOCK` | `false` en Docker, `true` en dev | `true` = API simulada |
| `VITE_API_URL` | vacío (mismo origen) | Base de la API si no va detrás del proxy |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | Solo dev: destino del proxy de Vite |
| `VITE_TILE_URL` | `https://tile.openstreetmap.org/{z}/{x}/{y}.png` | Servidor de teselas del mapa (ver nota del mapa) |
| `VITE_ADMIN_URL` · `VITE_CLIENT_URL` | `http://localhost:5176` · `:5175` | Enlaces cruzados entre el portal y el centro de mando |

**Docker (una imagen por front):**

```bash
docker build --build-arg APP=preventista   -t dulcemayor-preventista .
docker build --build-arg APP=transportista -t dulcemayor-transportista .
docker build --build-arg APP=cliente       -t dulcemayor-cliente .
docker build --build-arg APP=admin         -t dulcemayor-admin .
docker compose -f docker-compose.frontend.yml up --build   # prueba local con mock: :8081 a :8084
```

**Integración al `docker-compose.yml` productivo (DonWeb VPS)** — servicios a sumar junto a PostgreSQL y backend:

```yaml
  preventista:
    build: { context: ./frontend, args: { APP: preventista } }
    restart: unless-stopped
  transportista:
    build: { context: ./frontend, args: { APP: transportista } }
    restart: unless-stopped
  cliente:
    build: { context: ./frontend, args: { APP: cliente } }
    restart: unless-stopped
  admin:
    build: { context: ./frontend, args: { APP: admin } }
    restart: unless-stopped
```

Caddyfile del reverse proxy (un subdominio por front, HTTPS automático):

```
preventista.dulcemayor.com.ar   { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy preventista:80 } }
reparto.dulcemayor.com.ar       { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy transportista:80 } }
www.dulcemayor.com.ar           { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy cliente:80 } }
admin.dulcemayor.com.ar         { handle /api/* { reverse_proxy backend:8080 }  handle { reverse_proxy admin:80 } }
```

## Decisiones a tener en cuenta

- **Login con Google (cliente)**: el botón redirige al flujo OAuth2 del backend; en modo demo no funciona. El texto comercial del Hero ("+2.000 productos", "Envío gratis desde $150.000") es de ese front original: validarlo con el negocio.

- **JWT en `sessionStorage`**: simple y sin CSRF, pero expuesto a XSS. Para producción endurecida, el backend puede emitir una cookie `HttpOnly; Secure; SameSite=Strict` y se elimina el manejo del token en `http.js`.
- **Mapa y rutas**: `MapView.jsx` usa Leaflet con teselas de OpenStreetMap, dentro de la app (pines numerados, ruta punteada, atribución obligatoria de OSM); las teselas las pide el navegador (`VITE_TILE_URL` para otro proveedor). **Todo el ruteo lo hace el backend**: "Cómo llegar" (`Directions.jsx`) pide el trayecto a `GET /api/ruteo/trayecto`; "Optimizar mi ruta" (preventista) y "Optimizar mi reparto" (transportista) envían la ubicación actual a `POST /api/rutas/{id}/optimizar`, y el backend ordena por calles con OSRM (respaldo en línea recta), guarda el orden y devuelve el recorrido para dibujarlo. Al confirmar un pedido, `OrderDonePage.jsx` pide lo mismo desde el comercio recién visitado y propone el siguiente punto más eficiente. El orden del transportista no cambia solo tras cada entrega, porque el aviso por correo al próximo comercio depende de él. El servidor OSRM se configura en el backend (`APP_OSRM_URL`; el público es solo para desarrollo). Con `VITE_USE_MOCK=true` el mock simula esos endpoints con una estimación en línea recta (el plan sale como `estimada`). Sin permiso de ubicación se usa un origen de demo. Los datos de detalle de entrega, pedidos y puntos de venta siguen contra el mock hasta que el backend los exponga.
- **Llamar al cliente**: solo existe en el preventista (`tel:` en el swipe de la tarjeta). El transportista no tiene ni el botón ni los teléfonos.