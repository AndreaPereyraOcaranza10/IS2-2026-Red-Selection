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

## Creado nuevo (estilo RG)
Los tres runners son idempotentes y se ejecutan en este orden: `CatalogoInicial` y `UsuariosDemo` (`@Order(1)`), luego `ProductosDemo` (`@Order(2)`).
- `config/CatalogoInicial`: siembra Niños, Niñas, Mujeres y Hombres, cada una con Ropa, Calzado y Accesorios.
- `config/UsuariosDemo`: crea un usuario ADMINISTRATIVO y uno JEFE (ver credenciales abajo).
- `config/ProductosDemo`: crea 8 productos de ejemplo (3 en oferta: CAL-002, ACC-005 y ZAP-007; los otros 5 sin oferta), cada uno con imagen tomada de `static/tienda/images/products/` y dos vigencias de precio (una anterior ya cerrada y la vigente, con `fechaHasta` nula).

## Credenciales de demostración
| Rol | Usuario | Clave |
|---|---|---|
| ADMINISTRATIVO | `administrativo@tiendazero.com` | `admin1234` |
| JEFE | `jefe@tiendazero.com` | `jefe1234` |
| JEFE (ya existía en RG, vía `app.admin.*`) | `admin@tiendazero.com` | `1234` |

Se pueden cambiar agregando en `application.properties`: `app.demo.administrativo.usuario`, `app.demo.administrativo.clave`, `app.demo.jefe.usuario` y `app.demo.jefe.clave`. Son datos de prueba: no usar en producción.

## Adaptaciones a RG
- `SecurityConfig`: `/inventory/**`, `/reports/**`, `/docs/**` y `/products/**` requieren ADMINISTRATIVO o JEFE (igual que `/admin/**`); `/checkout/**` requiere estar autenticado.
- Vistas: `/register` → `/registro`; `hasRole('ADMIN')` → `hasAnyRole('ADMINISTRATIVO','JEFE')`; "Mi Perfil" solo para CLIENTE y apunta a `/completar-perfil`.
- `admin/create-product.html`: el token CSRF va en la URL del `th:action`, porque RG tiene CSRF activo y en un formulario multipart el filtro no lo lee del cuerpo.
- Se quitó del menú lateral admin el bloque "Account" (Log in / Sign up).

## Descartado (RG prevalece o fuera de alcance)
`AuthController`, `PerfilController`, `PerfilService(Impl)`, `EmailService(Impl)`, `CustomUserDetailsService`, `CustomAuthentication*Handler`, `DataInitializer`, `Rol`, `Usuario`/`Persona` de RT, DTOs de auth/perfil, plantillas `admin/signin`, `admin/signup`, `tienda/auth/*`, `tienda/cliente/perfil`. `pom.xml` y las claves existentes de `application.properties` no se tocaron.

## Pendiente / a tener en cuenta
- `cart.html` y `checkout.html` son solo maquetas: no existen `Factura`, `DetalleFactura`, `OrdenCompra`, `Carrito` ni `Stock` en ninguno de los dos proyectos.
- Los productos de ejemplo se crean solos al arrancar. Para cargar más, usar `/products/new`; el precio se guarda como `VigenciaPrecio`.
- Los estados de la home (`inStock`, `rating`, `reviewCount`) son valores fijos de RT, no datos reales.
- `application.properties` de RG tiene la contraseña de Gmail en texto plano: pasarla a variable de entorno.
