# Dockerfile multi-stage del backend (Spring Boot, Java 21).
#   docker build -t dulcemayor-backend .
# Los tests se corren en Jenkins (etapa "Backend: tests"); acá solo se empaqueta.

# ---------- 1) build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Capa de dependencias (cacheable): solo cambia si cambia el pom.xml
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q -DskipTests package

# ---------- 2) runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Usuario sin privilegios: el proceso no corre como root dentro del contenedor
RUN useradd --system --uid 1001 dulcemayor
COPY --from=build /app/target/*SNAPSHOT.jar app.jar
USER dulcemayor
EXPOSE 8080
ENV TZ=America/Argentina/Mendoza
# Chequeo con el catálogo público: confirma que la app responde Y que llega a la base.
# (/actuator/health da DOWN si no hay servidor de correo configurado, y liveness/readiness piden sesión.)
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/api/products || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
