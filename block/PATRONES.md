# Patrones y responsabilidades de la versión personal

## Strategy

`IdentityStrategy` separa las dos formas de autenticar una cuenta:

- `LocalIdentityStrategy` valida email, estado activo y contraseña BCrypt.
- `GoogleIdentityStrategy` recibe una identidad OIDC ya verificada por Spring Security, busca por proveedor/sub o crea un usuario USER. No vincula automáticamente una cuenta local por coincidencia de email.

`UsuarioService.login` usa la estrategia local; el handler OAuth usa la estrategia Google después de verificar el email.

## Perfil y persistencia

Repository y DTO/Mapper separan persistencia y API. `UsuarioService` obtiene la identidad del contexto de seguridad, valida la localidad activa y sus antecesores, y persiste únicamente el perfil de esa cuenta dentro de una transacción.

Si la Persona o Dirección estaba compartida, aplica copia antes de modificarla para preservar los datos de la otra cuenta. El versionado de credenciales permite invalidar JWT anteriores al cambiar la contraseña.

## Catálogo de solo lectura

`CatalogoGeograficoService` devuelve DTO específicos con nombres, códigos postales e ids de padres. No incluye personas ni domicilios. No existe una operación HTTP para modificar ese catálogo: los datos se mantienen mediante scripts SQL versionados con Flyway.

El Template Method de los antiguos ABM se retiró junto con sus servicios y controladores, porque esos módulos ya no forman parte de esta aplicación.
