# Defensa de la aplicación de cuenta personal

1. **Alcance:** cada usuario accede a Mi Perfil, Cambiar Contraseña y Cerrar Sesión. El inicio abre Mi Perfil. No hay dashboard ni pantallas o endpoints ABM.
2. **Capas:** React presenta los formularios; los controladores reciben HTTP; los servicios implementan reglas y transacciones; los repositorios acceden a JPA. La API devuelve DTO y no entidades.
3. **Modelo:** se mantienen las relaciones País → Provincia → Departamento → Localidad → Dirección → Persona para guardar una dirección válida. Usuario tiene una Persona opcional y no se confunde con ella.
4. **Precarga:** Flyway ejecuta V2 para incorporar un catálogo geográfico de ejemplo idempotente. V3 convierte las antiguas cuentas ADMIN a USER e invalida sus tokens sin borrar datos personales.
5. **Privacidad:** la API obtiene el usuario de Principal. No acepta ids de cuenta en el formulario y no permite consultar listados de personas o direcciones ajenas. Los antiguos endpoints administrativos están bloqueados incluso para cuentas heredadas con rol ADMIN.
6. **Mi Perfil:** permite completar datos aunque inicialmente no exista Persona o Dirección. Los selectores usan un catálogo autenticado de solo lectura y limpian sus descendientes cuando cambia un antecesor. El backend verifica toda la jerarquía activa.
7. **JWT:** firma HS256 con secreto externo, emisor y vencimiento verificados. Cada solicitud comprueba que la cuenta siga activa y que la versión de credenciales coincida. El navegador conserva el token en sessionStorage.
8. **OAuth2:** Google usa Authorization Code/OIDC y state. Spring valida la identidad y el handler exige email verificado; después emite el JWT y abre Mi Perfil. El callback autorizado en Google continúa siendo `/login/oauth2/code/google`.
9. **Contraseña:** BCrypt verifica la contraseña actual. La nueva debe tener letras y números, mínimo 8 caracteres, máximo 72 bytes, confirmación coincidente y ser diferente. Incrementar credentialVersion revoca los tokens anteriores. Una cuenta exclusivamente Google muestra autenticación externa.
10. **Patrones:** Strategy diferencia autenticación local y Google. La modificación del perfil aplica copia si los datos eran compartidos. El Template Method administrativo se eliminó con los ABM.
11. **Despliegue:** Docker Compose ejecuta PostgreSQL, Spring Boot y frontend/Caddy. Flyway migra y Hibernate valida. Caddy sirve la SPA y el proxy; un dominio real habilita su configuración HTTPS. README detalla credenciales Google, callbacks y ejecución.

Las pruebas y sus límites actuales están documentados en VERIFICACION.md. El intercambio real con Google requiere un cliente propio configurado en Google Cloud.
