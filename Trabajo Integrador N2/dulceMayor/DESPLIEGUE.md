# Despliegue (Docker) e integración continua (Jenkins)

## Docker: levantar todo el sistema

Requisito: Docker Desktop abierto.

```bash
cd "Trabajo Integrador N2/dulceMayor"
docker compose up -d --build
```

La primera vez tarda varios minutos (descarga imágenes, compila el backend y las 4 apps). Después:

| Servicio | URL | Imagen |
|---|---|---|
| Preventista | http://localhost:8081 | `dulcemayor-preventista` |
| Transportista | http://localhost:8082 | `dulcemayor-transportista` |
| Sitio institucional (cliente) | http://localhost:8083 | `dulcemayor-cliente` |
| Administración | http://localhost:8084 | `dulcemayor-admin` |
| API REST (directo) | http://localhost:8090/api | `dulcemayor-backend` |
| Base de datos (MariaDB 10.11) | `localhost:3307` | `mariadb:10.11` |

- Cada app es un nginx que sirve los archivos compilados y reenvía `/api` al backend por la red interna de Docker.
- La base arranca vacía y el backend crea las tablas y los **datos de demostración** (cuentas `jefe.mza`, `admin.mza`, `preventista.mza`, `transportista.mza`, `cliente.demo`; clave inicial `dulcemayor`).
- Los datos quedan en el volumen `db_data`: sobreviven a `docker compose down`. Para empezar de cero: `docker compose down -v`.
- Los puertos no chocan con el entorno de desarrollo (Vite 5173–5176, backend 8080, XAMPP 3306): se pueden tener los dos a la vez.

Comandos útiles:

```bash
docker compose ps                 # estado (backend "healthy" = listo)
docker compose logs -f backend    # logs del backend
docker compose down               # apagar
```

### Variables opcionales

Sin configuración, todo funciona con valores de demo: pagos con la pasarela simulada, sin envío de correos y sin ingreso con Google. Para activarlos, copiar `.env.example` a `.env` (en esta carpeta) y completar. El `.env` está en `.gitignore`.

- **Google (Auth0):** en el panel de Auth0 hay que agregar `http://localhost:8083` como URL de callback, de logout y origen permitido.
- **Asistente de presupuestos:** usa el Ollama instalado en la PC (`host.docker.internal:11434`).

## Jenkins: integración continua

El pipeline está en [`Jenkinsfile`](Jenkinsfile). Etapas:

1. **Backend: compilar y testear** — `mvn test` dentro de `maven:3.9-eclipse-temurin-21`. Publica los resultados JUnit.
2. **Frontend: testear y compilar** — `npm ci`, `npm test` (Vitest) y `npm run build` dentro de `node:20-alpine`.
3. **Imágenes Docker** — `docker compose build`.
4. **Despliegue** — `docker compose up -d` (en un pipeline multibranch, solo en la rama `main`).

### Configurar el job

1. Plugins: *Pipeline*, *Git*, *Docker Pipeline* y *JUnit*.
2. El agente necesita Docker y `docker compose` (Java, Maven y Node no: corren en contenedores).
3. Nuevo item → *Pipeline* → *Pipeline script from SCM* → Git con la URL del repositorio.
4. **Script Path:** `Trabajo Integrador N2/dulceMayor/Jenkinsfile`.

Para correr Jenkins localmente con Docker:

```bash
docker run -d --name jenkins -p 9090:8080 -v jenkins_home:/var/jenkins_home -v //var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts-jdk21
```

(La imagen oficial no trae el cliente de Docker: hay que instalarlo dentro del contenedor o usar un agente que lo tenga.)
