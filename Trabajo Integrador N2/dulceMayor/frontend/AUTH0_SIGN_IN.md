# Acceso con Google mediante Auth0

El inicio y registro con Google se realiza en Auth0 Universal Login. Auth0 se ocupa del proveedor Google; el backend valida el ID token de Auth0 y emite el JWT propio de Dulce Mayor. El login por correo y clave del portal continúa disponible.

## Configuración de Auth0

1. Crea una aplicación **Single Page Application** en Auth0.
2. Habilita la conexión social de Google y actívala para esa aplicación. El identificador de conexión predeterminado usado por el portal es `google-oauth2`.
3. En Allowed Callback URLs agrega `http://localhost:5175/ingresar` y `http://127.0.0.1:5175/ingresar`.
4. En Allowed Web Origins y Allowed Logout URLs agrega `http://localhost:5175` y `http://127.0.0.1:5175`.
5. Copia el dominio del tenant y el Client ID en `frontend/.env.development` como `VITE_AUTH0_DOMAIN` y `VITE_AUTH0_CLIENT_ID`.
6. Configura los mismos valores en el archivo ignorado `backend/.env` como `AUTH0_DOMAIN` y `AUTH0_CLIENT_ID`.

No se necesita Client Secret en el SPA ni en el backend para este flujo. Reinicia Vite y el backend tras cambiar los archivos de entorno. En producción, registra también los orígenes de producción en Auth0 y configura sus variables de entorno.

La app cliente envía el ID token a `POST /api/auth/auth0`. El backend comprueba firma con las claves públicas JWKS del tenant, emisor, audiencia, expiración y correo verificado antes de crear una cuenta o emitir la sesión propia de Dulce Mayor. Las cuentas ya creadas por el anterior login directo de Google se vinculan a su identidad Auth0 al ingresar con el mismo correo verificado.
