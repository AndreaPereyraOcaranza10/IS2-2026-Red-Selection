# DulceMayor · Integrador 2

El proyecto está separado en dos aplicaciones:

- `backend/`: API REST Java 21 con Spring Boot, JPA y Spring Security.
- `frontend/`: portal React y TypeScript.

Los datos locales de H2 se conservan en `data/`, fuera del código de ambas aplicaciones. La configuración del backend apunta a esa carpeta para mantener la base existente.

## Ejecutar en desarrollo

Instalá las dependencias del frontend una vez con `cd frontend` y `pnpm install`.

En una terminal, desde la raíz:

```powershell
cd backend
.\mvnw.cmd clean spring-boot:run
```

En otra terminal:

```powershell
cd frontend
pnpm dev
```

El portal queda en `http://localhost:5173` y la API en `http://localhost:8080`. Vite envía `/api` al backend. En macOS/Linux, usar `./mvnw clean spring-boot:run`.

Para MySQL, definir `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` antes de iniciar Spring Boot. La cuenta demo local es `admin@dulcemayor.com` / `dulcemayor`.

## API principal

- `GET /api/health`, `GET /api/products?search=&category=`, `GET /api/products/{id}`, `GET /api/categories`
- `POST /api/auth/login`, `POST /api/auth/register`, `POST /api/auth/activate`, `GET /api/auth/me`, `POST /api/auth/logout`
- Con sesión y rol autorizado: `/api/admin/products` (GET/POST), `/api/admin/products/{id}` (PUT/DELETE), `/api/admin/products/{id}/stock` (PATCH), `POST /api/admin/images`
- Con sesión: `GET/POST /api/orders`; el estado se actualiza por `PATCH /api/orders/{id}/status` para personal autorizado.

Los emails de activación se simulan y se muestran en la consola en este entorno. El catálogo inicial y las sucursales son datos de demostración.
