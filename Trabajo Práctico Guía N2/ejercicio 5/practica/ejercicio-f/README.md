# Seguridad con API

**Método elegido: autenticación con JWT (Bearer token) + autorización por roles, con Spring Security.**

## Qué se agregó

**servidor/**
- `entities/Usuario`, `Rol`, `repositories/UsuarioRepository`: usuarios en la BD, contraseña hasheada con **BCrypt**.
- `security/SecurityConfig`: reglas por endpoint y rol, sin sesión (stateless), respuestas 401/403 en JSON.
- `security/JwtService`: genera y valida tokens (HS256, vencen a los 60 min).
- `security/JwtAuthenticationFilter`: lee el header `Authorization` en cada request.
- `security/AuthController`: `/login` y `/register` (el registro siempre crea rol USER).
- `security/AdminInitializer`: crea el usuario `admin` al primer arranque.
- `.env`: se agregaron `JWT_SECRET` y `ADMIN_PASSWORD` (el secreto **no** está en el código). Ver `.env.example`.

**cliente/**
- Login contra la API (`ApiAuthenticationProvider`), el token queda en la sesión del servidor del cliente.
- `JwtBearerInterceptor`: agrega el token a cada llamada del `RestTemplate`.
- Pantalla `/login`, botón "Salir", CSRF activo en los formularios, mensajes claros para 401/403.

