FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B verify

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl && addgroup -S atlas && adduser -S atlas -G atlas
WORKDIR /app
COPY --from=build /app/target/abm-1.0.0.jar app.jar
USER atlas
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=5 CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","app.jar"]
