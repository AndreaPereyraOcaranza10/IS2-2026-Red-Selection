# Datos de prueba

Al iniciar el backend, `DevelopmentDataInitializer` crea categorias y productos de ejemplo, ademas de dos cuentas si sus correos todavia no existen.

| Perfil | Email | Contrasena |
| --- | --- | --- |
| Administrador (`ADMIN`) | `admin@shopease.local` | `Admin123!` |
| Cliente (`USER`) | `cliente@shopease.local` | `Cliente123!` |

Los datos se guardan en la base MySQL configurada en `application.properties`. Las imagenes del catalogo se cargan desde URLs de Unsplash, por lo que requieren conexion a Internet en el navegador.

Estas credenciales son unicamente para desarrollo local. Cambia o elimina las cuentas antes de desplegar la aplicacion en un entorno real. El backend actualmente asigna los roles a las cuentas, pero sus endpoints de escritura no comprueban el rol `ADMIN`.
