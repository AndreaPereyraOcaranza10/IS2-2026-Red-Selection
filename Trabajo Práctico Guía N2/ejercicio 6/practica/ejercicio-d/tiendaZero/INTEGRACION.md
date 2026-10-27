# Integración RT → RG (tiendaZero)

**Base:** `tiendaZero_RG`. **Alcance tomado de RT:** vistas y parte de catálogo/tienda.
El flujo de login, registro, activación y perfil queda como en RG. Cada paso tiene su commit (`git log`).

## Incorporado desde RT
| Tipo | Elementos |
|---|---|
| Controladores | `HomeController`, `TiendaController`, `AdminController` (sin `/admin/signin` ni `/admin/signup`), `ImagenController` |
| Config | `WebMvcConfig` (mapea `/assets/**`, `/tienda/**`, `/admin/**` a `static/`) |
| DTO | `ProductoCardDTO`, `ProductoFormDTO`, `ProductoInventarioDTO` |
| Mejoras en clases de RG | `Categoria` (`getName()`, `getSlug()`, `@Builder.Default`), `SubCategoria` y `Producto` (`@Builder.Default`), `SubCategoriaRepository.findByEliminadoFalse()`, `ProductoServiceImpl` tolera `idImagen` nulo |
| Vistas | `tienda/`: index, shop, single-product-page, cart, checkout, error/404, fragments. `admin/`: index, inventory, create-product, reports, docs, 404-error, fragments |
| Estáticos | `static/tienda/**` y `static/admin/**` (el `static/css/estilos.css` de RG se conserva) |
| Config nueva | `spring.servlet.multipart.max-file-size=5MB` y `max-request-size=6MB` |

## Datos iniciales
Los cargadores de datos en Java se reemplazaron por `src/main/resources/data.sql`. Ejecutalo manualmente una vez, después de que Spring/Hibernate haya creado las tablas, usando MySQL Workbench y la base `tienda_zero`. El script incluye cuentas, catálogo, geografía, proveedores, productos con precios e inventario, empresas y empleados, órdenes y facturas.

## Credenciales de demostración
| Rol | Usuario | Clave |
|---|---|---|
| ADMINISTRATIVO | `administrativo@tiendazero.com` | `admin1234` |
| JEFE | `jefe@tiendazero.com` | `jefe1234` |
| JEFE | `admin@tiendazero.com` | `1234` |

Son credenciales de desarrollo, no usar en producción.

## Adaptaciones a RG
- `SecurityConfig`: `/inventory/**`, `/reports/**`, `/docs/**` y `/products/**` requieren ADMINISTRATIVO o JEFE (igual que `/admin/**`); `/checkout/**` requiere estar autenticado.
- Vistas: `/register` → `/registro`; `hasRole('ADMIN')` → `hasAnyRole('ADMINISTRATIVO','JEFE')`; "Mi Perfil" solo para CLIENTE y apunta a `/completar-perfil`.
- `admin/create-product.html`: el token CSRF va en la URL del `th:action`, porque RG tiene CSRF activo y en un formulario multipart el filtro no lo lee del cuerpo.
- Se quitó del menú lateral admin el bloque "Account" (Log in / Sign up).

## Descartado (RG prevalece o fuera de alcance)
`AuthController`, `PerfilController`, `PerfilService(Impl)`, `EmailService(Impl)`, `CustomUserDetailsService`, `CustomAuthentication*Handler`, `DataInitializer`, `Rol`, `Usuario`/`Persona` de RT, DTOs de auth/perfil, plantillas `admin/signin`, `admin/signup`, `tienda/auth/*`, `tienda/cliente/perfil`. `pom.xml` no se tocó.

## Pendiente / a tener en cuenta
- `cart.html` y `checkout.html` son solo maquetas: no existen `Factura`, `DetalleFactura`, `OrdenCompra`, `Carrito` ni `Stock` en ninguno de los dos proyectos.
- Los datos iniciales se cargan ejecutando `data.sql`; para productos nuevos, usar `/products/new`. El precio se guarda como `VigenciaPrecio`.
- Los estados de la home (`inStock`, `rating`, `reviewCount`) son valores fijos de RT, no datos reales.
- `application.properties` de RG tiene la contraseña de Gmail en texto plano: pasarla a variable de entorno.
