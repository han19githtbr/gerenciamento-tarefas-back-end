# =============================================
# STAGE 1: Build
# =============================================
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixa dependencias antes de copiar o codigo (cache de layers)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# =============================================
# STAGE 2: Runtime
# =============================================
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/desafio-0.0.1-SNAPSHOT.jar desafio.jar

# O Render injeta $PORT dinamicamente - nao fixar a porta aqui
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "desafio.jar"]