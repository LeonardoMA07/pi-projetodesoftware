# Build stage
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

# Copia o pom primeiro para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn dependency:resolve

# Copia o codigo fonte
COPY src src

# Build da aplicacao (sem testes, eles rodam no pipeline)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
