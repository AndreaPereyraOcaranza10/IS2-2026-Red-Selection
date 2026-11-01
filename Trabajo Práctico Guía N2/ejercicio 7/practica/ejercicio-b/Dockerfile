FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -ntp package -Dmaven.test.skip=true

FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
ARG APP_MODULE
ARG APP_PORT
COPY --from=build /workspace/${APP_MODULE}/target/*.jar app.jar
EXPOSE ${APP_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
