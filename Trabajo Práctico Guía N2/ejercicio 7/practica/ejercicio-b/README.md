# Curso de microservicios

Proyecto de ecommerce con Spring Boot, Spring Cloud, bases de datos separadas por dominio y despliegue con Docker Compose.

## Ejecutar el sistema

Requisitos: Docker Desktop iniciado y soporte de contenedores Linux. Desde la raíz:

```powershell
docker compose config
docker compose up --build -d
docker compose ps
```

La primera compilación descarga imágenes y dependencias y puede demorar varios minutos. Interfaz de Eureka: <http://localhost:8761>. API Gateway: <http://localhost:8222>. Prometheus: <http://localhost:9090>. Grafana: <http://localhost:3000>.

Para detener los servicios conservando los datos:

```powershell
docker compose down
```

Para eliminar también los datos persistidos de MongoDB, PostgreSQL y Grafana:

```powershell
docker compose down -v
```

## Compilar con Maven en el host

Se requiere JDK 25 y Maven instalado. Desde la raíz:

```powershell
mvn clean package -DskipTests
```

## Playlist

- [Introducción y configuración](https://youtu.be/rC5ES1vmRmc?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Config Server](https://youtu.be/D2iwCEKpUws?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Discovery Server con Eureka](https://youtu.be/MBBl6lIFvPQ?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Microservicio de clientes](https://youtu.be/CBAVpdqEa4U?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Manejo de excepciones](https://youtu.be/FOiGQA1mMTM?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Logging con Logback](https://youtu.be/AFStwdGYHHM?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Microservicio de productos](https://youtu.be/8G4PzU_4Jzw?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [Docker Compose](https://youtu.be/UQoSrMn96TI?list=PLx89vzy-Ta0pvP5yEdr4KPePSQREJywa4)
- [OpenFeign](https://youtu.be/pMnC6VpsLWY)

Consulta [GUIA_TEORIA_Y_EJECUCION.md](GUIA_TEORIA_Y_EJECUCION.md) para la arquitectura, comandos y ejemplos de pruebas HTTP.
