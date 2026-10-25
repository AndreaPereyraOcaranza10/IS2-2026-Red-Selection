# ===== Stage 1: Build the application =====
FROM maven:3.8.7-eclipse-temurin-17 AS build

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# ===== Stage 2: Run the application =====
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/target/CatalogueManagement-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
